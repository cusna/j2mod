/*
 * This file is part of j2mod.
 *
 * j2mod is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * j2mod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with j2mod.  If not, see <http://www.gnu.org/licenses
 */
package com.j2mod.modbus.cmd;

import com.j2mod.modbus.ModbusException;
import com.j2mod.modbus.io.*;
import com.j2mod.modbus.msg.*;
import com.j2mod.modbus.net.ModbusMasterFactory;
import com.j2mod.modbus.procimg.Register;
import com.j2mod.modbus.util.Logger;

import java.io.IOException;
import java.util.Arrays;

/**
 * Class that implements a simple command line tool for writing to an analog
 * output over a Modbus/TCP connection.
 *
 * <p>
 * Note that if you write to a remote I/O with a Modbus protocol stack, it will
 * most likely expect that the communication is <i>kept alive</i> after the
 * first write message.
 *
 * <p>
 * This can be achieved either by sending any kind of message, or by repeating
 * the write message within a given period of time.
 *
 * <p>
 * If the time period is exceeded, then the device might react by turning off
 * all signals of the I/O modules. After this timeout, the device might require
 * a reset message.
 *
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 *
 * @author Julie Haugh
 * @version 1.03 (1/18/2014)
 */
public class ReadHoldingRegistersTest {

    private static final Logger logger = Logger.getLogger(ReadHoldingRegistersTest.class);

    private static void printUsage() {
        logger.system("\nUsage:\n    java com.j2mod.modbus.cmd.ReadHoldingRegistersTest <address{:port{:unit}} [String]> <base [int]> <count [int]> {<repeat [int]>}");
    }

    public static void main(String[] args) {
        ModbusTransport transport = null;
        ModbusRequest req;
        ModbusTransaction trans;
        int ref = 0;
        int count = 0;
        int repeat = 1;
        int unit = 0;

        // 1. Setup parameters
        if (args.length < 3) {
            printUsage();
            System.exit(1);
        }

        try {
            try {
                // 2. Open the connection.
                transport = ModbusMasterFactory.createModbusMaster(args[0]);

                if (transport == null) {
                    logger.system("Cannot open %s", args[0]);
                    System.exit(1);
                }

                if (transport instanceof ModbusSerialTransport) {
                    ((ModbusSerialTransport)transport).setReceiveTimeout(500);
                    if (System.getProperty("com.j2mod.modbus.baud") != null) {
                        ((ModbusSerialTransport)transport).setBaudRate(Integer.parseInt(System.getProperty("com.j2mod.modbus.baud")));
                    }
                    else {
                        ((ModbusSerialTransport)transport).setBaudRate(19200);
                    }
                }

				/*
                 * There are a number of devices which won't initialize immediately
				 * after being opened.  Take a moment to let them come up.
				 */
                Thread.sleep(2000);

                ref = Integer.parseInt(args[1]);
                count = Integer.parseInt(args[2]);

                if (args.length == 4) {
                    repeat = Integer.parseInt(args[3]);
                }

                if (transport instanceof ModbusTCPTransport) {
                    String parts[] = args[0].split(" *: *");
                    if (parts.length >= 4) {
                        unit = Integer.parseInt(parts[3]);
                    }
                }
                else if (transport instanceof ModbusRTUTransport) {
                    String parts[] = args[0].split(" *: *");
                    if (parts.length >= 3) {
                        unit = Integer.parseInt(parts[2]);
                    }

                    String baud = System.getProperty("com.j2mod.modbus.baud");
                    if (baud != null) {
                        ((ModbusRTUTransport)transport).setBaudRate(Integer.parseInt(baud));
                    }
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                printUsage();
                System.exit(1);
            }

            // 3. Create the command.
            req = new ReadMultipleRegistersRequest(ref, count);
            req.setUnitID(unit);

            // 4. Prepare the transaction
            trans = transport.createTransaction();
            trans.setRequest(req);
            req.setHeadless(trans instanceof ModbusSerialTransaction);

            logger.system("Request: %s", req.getHexMessage());

            // 5. Execute the transaction repeat times

            for (int i = 0; i < repeat; i++) {
                try {
                    trans.execute();
                }
                catch (ModbusException x) {
                    logger.system(x.getMessage());
                    continue;
                }
                ModbusResponse res = trans.getResponse();
                if (res != null) {
                    logger.system("Response: %s", res.getHexMessage());
                }
                else {
                    logger.system("No response to READ HOLDING request");
                }
                if (res instanceof ExceptionResponse) {
                    ExceptionResponse exception = (ExceptionResponse)res;
                    logger.system(exception.toString());
                    continue;
                }

                if (!(res instanceof ReadMultipleRegistersResponse)) {
                    continue;
                }

                ReadMultipleRegistersResponse data = (ReadMultipleRegistersResponse)res;
                Register values[] = data.getRegisters();

                logger.system("Data: %s", Arrays.toString(values));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            // 6. Close the connection
            if (transport != null) {
                transport.close();
            }
        }
        catch (IOException e) {
            // Do nothing.
        }
        System.exit(0);
    }
}