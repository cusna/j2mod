/*
 * Copyright 2002-2016 jamod & j2mod development teams
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.j2mod.modbus.facade;

import com.j2mod.modbus.ModbusException;
import com.j2mod.modbus.io.ModbusTransaction;
import com.j2mod.modbus.msg.*;
import com.j2mod.modbus.procimg.InputRegister;
import com.j2mod.modbus.procimg.Register;
import com.j2mod.modbus.util.BitVector;
import com.j2mod.modbus.util.Logger;

/**
 * Modbus/TCP Master facade.
 *
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 *
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 *
 */
abstract public class AbstractModbusMaster {

    private static final Logger logger = Logger.getLogger(AbstractModbusMaster.class);
    private static final int DEFAULT_UNIT_ID = 1;

    protected ModbusTransaction m_Transaction;
    private ReadCoilsRequest m_ReadCoilsRequest;
    private ReadInputDiscretesRequest m_ReadInputDiscretesRequest;
    private WriteCoilRequest m_WriteCoilRequest;
    private WriteMultipleCoilsRequest m_WriteMultipleCoilsRequest;
    private ReadInputRegistersRequest m_ReadInputRegistersRequest;
    private ReadMultipleRegistersRequest m_ReadMultipleRegistersRequest;
    private WriteSingleRegisterRequest m_WriteSingleRegisterRequest;
    private WriteMultipleRegistersRequest m_WriteMultipleRegistersRequest;

    /**
     * Initialises all the objects needed by this connection
     */
    public AbstractModbusMaster() {
        m_ReadCoilsRequest = new ReadCoilsRequest();
        m_ReadInputDiscretesRequest = new ReadInputDiscretesRequest();
        m_WriteCoilRequest = new WriteCoilRequest();
        m_WriteMultipleCoilsRequest = new WriteMultipleCoilsRequest();
        m_ReadInputRegistersRequest = new ReadInputRegistersRequest();
        m_ReadMultipleRegistersRequest = new ReadMultipleRegistersRequest();
        m_WriteSingleRegisterRequest = new WriteSingleRegisterRequest();
        m_WriteMultipleRegistersRequest = new WriteMultipleRegistersRequest();
    }

    /**
     * Sets the transaction to use
     * @param transaction Transaction to use
     */
    protected void setTransaction(ModbusTransaction transaction) {
        m_Transaction = transaction;
    }

    /**
     * Connects this <tt>ModbusTCPMaster</tt> with the slave.
     *
     * @throws Exception if the connection cannot be established.
     */
    abstract public void connect() throws Exception;

    /**
     * Disconnects this <tt>ModbusTCPMaster</tt> from the slave.
     */
    abstract public void disconnect();

    /**
     * Reads a given number of coil states from the slave.
     * <p/>
     * Note that the number of bits in the bit vector will be
     * forced to the number originally requested.
     *
     * @param unitId the slave unit id.
     * @param ref   the offset of the coil to start reading from.
     * @param count the number of coil states to be read.
     *
     * @return a <tt>BitVector</tt> instance holding the
     * received coil states.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public synchronized BitVector readCoils(int unitId, int ref, int count) throws ModbusException {
        m_ReadCoilsRequest.setUnitID(unitId);
        m_ReadCoilsRequest.setReference(ref);
        m_ReadCoilsRequest.setBitCount(count);
        m_Transaction.setRequest(m_ReadCoilsRequest);
        m_Transaction.execute();
        BitVector bv = ((ReadCoilsResponse)m_Transaction.getResponse()).getCoils();
        bv.forceSize(count);
        return bv;
    }

    /**
     * Writes a coil state to the slave.
     *
     * @param unitId the slave unit id.
     * @param ref    the offset of the coil to be written.
     * @param state  the coil state to be written.
     *
     * @return the state of the coil as returned from the slave.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public synchronized boolean writeCoil(int unitId, int ref, boolean state) throws ModbusException {
        m_WriteCoilRequest.setUnitID(unitId);
        m_WriteCoilRequest.setReference(ref);
        m_WriteCoilRequest.setCoil(state);
        m_Transaction.setRequest(m_WriteCoilRequest);
        m_Transaction.execute();
        return ((WriteCoilResponse)m_Transaction.getResponse()).getCoil();
    }

    /**
     * Writes a given number of coil states to the slave.
     * <p/>
     * Note that the number of coils to be written is given
     * implicitly, through {@link BitVector#size()}.
     *
     * @param unitId the slave unit id.
     * @param ref   the offset of the coil to start writing to.
     * @param coils a <tt>BitVector</tt> which holds the coil states to be written.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public synchronized void writeMultipleCoils(int unitId, int ref, BitVector coils) throws ModbusException {
        m_WriteMultipleCoilsRequest.setUnitID(unitId);
        m_WriteMultipleCoilsRequest.setReference(ref);
        m_WriteMultipleCoilsRequest.setCoils(coils);
        m_Transaction.setRequest(m_WriteMultipleCoilsRequest);
        m_Transaction.execute();
    }

    /**
     * Reads a given number of input discrete states from the slave.
     * <p/>
     * Note that the number of bits in the bit vector will be
     * forced to the number originally requested.
     *
     * @param unitId the slave unit id.
     * @param ref   the offset of the input discrete to start reading from.
     * @param count the number of input discrete states to be read.
     *
     * @return a <tt>BitVector</tt> instance holding the received input discrete
     * states.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public synchronized BitVector readInputDiscretes(int unitId, int ref, int count) throws ModbusException {
        m_ReadInputDiscretesRequest.setUnitID(unitId);
        m_ReadInputDiscretesRequest.setReference(ref);
        m_ReadInputDiscretesRequest.setBitCount(count);
        m_Transaction.setRequest(m_ReadInputDiscretesRequest);
        m_Transaction.execute();
        BitVector bv = ((ReadInputDiscretesResponse)m_Transaction.getResponse()).getDiscretes();
        bv.forceSize(count);
        return bv;
    }

    /**
     * Reads a given number of input registers from the slave.
     * <p/>
     * Note that the number of input registers returned (i.e. array length)
     * will be according to the number received in the slave response.
     *
     * @param unitId the slave unit id.
     * @param ref   the offset of the input register to start reading from.
     * @param count the number of input registers to be read.
     *
     * @return a <tt>InputRegister[]</tt> with the received input registers.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public synchronized InputRegister[] readInputRegisters(int unitId, int ref, int count) throws ModbusException {
        m_ReadInputRegistersRequest.setUnitID(unitId);
        m_ReadInputRegistersRequest.setReference(ref);
        m_ReadInputRegistersRequest.setWordCount(count);
        m_Transaction.setRequest(m_ReadInputRegistersRequest);
        m_Transaction.execute();
        return ((ReadInputRegistersResponse)m_Transaction.getResponse()).getRegisters();
    }

    /**
     * Reads a given number of registers from the slave.
     * <p/>
     * Note that the number of registers returned (i.e. array length)
     * will be according to the number received in the slave response.
     *
     * @param unitId the slave unit id.
     * @param ref   the offset of the register to start reading from.
     * @param count the number of registers to be read.
     *
     * @return a <tt>Register[]</tt> holding the received registers.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public synchronized Register[] readMultipleRegisters(int unitId, int ref, int count) throws ModbusException {
        m_ReadMultipleRegistersRequest.setUnitID(unitId);
        m_ReadMultipleRegistersRequest.setReference(ref);
        m_ReadMultipleRegistersRequest.setWordCount(count);
        m_Transaction.setRequest(m_ReadMultipleRegistersRequest);
        m_Transaction.execute();
        return ((ReadMultipleRegistersResponse)m_Transaction.getResponse()).getRegisters();
    }

    /**
     * Writes a single register to the slave.
     *
     * @param unitId the slave unit id.
     * @param ref      the offset of the register to be written.
     * @param register a <tt>Register</tt> holding the value of the register
     *                 to be written.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public synchronized void writeSingleRegister(int unitId, int ref, Register register) throws ModbusException {
        m_WriteSingleRegisterRequest.setUnitID(unitId);
        m_WriteSingleRegisterRequest.setReference(ref);
        m_WriteSingleRegisterRequest.setRegister(register);
        m_Transaction.setRequest(m_WriteSingleRegisterRequest);
        m_Transaction.execute();
    }

    /**
     * Writes a number of registers to the slave.
     *
     * @param unitId the slave unit id.
     * @param ref       the offset of the register to start writing to.
     * @param registers a <tt>Register[]</tt> holding the values of
     *                  the registers to be written.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public synchronized void writeMultipleRegisters(int unitId, int ref, Register[] registers) throws ModbusException {
        m_WriteMultipleRegistersRequest.setUnitID(unitId);;
        m_WriteMultipleRegistersRequest.setReference(ref);
        m_WriteMultipleRegistersRequest.setRegisters(registers);
        m_Transaction.setRequest(m_WriteMultipleRegistersRequest);
        m_Transaction.execute();
    }

    /**
     * Reads a given number of coil states from the slave.
     * <p/>
     * Note that the number of bits in the bit vector will be
     * forced to the number originally requested.
     *
     * @param ref   the offset of the coil to start reading from.
     * @param count the number of coil states to be read.
     *
     * @return a <tt>BitVector</tt> instance holding the
     * received coil states.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public synchronized BitVector readCoils(int ref, int count) throws ModbusException {
        return readCoils(DEFAULT_UNIT_ID, ref, count);
    }

    /**
     * Writes a coil state to the slave.
     *
     * @param ref    the offset of the coil to be written.
     * @param state  the coil state to be written.
     *
     * @return the state of the coil as returned from the slave.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public synchronized boolean writeCoil(int ref, boolean state) throws ModbusException {
        return writeCoil(DEFAULT_UNIT_ID, ref, state);
    }

    /**
     * Writes a given number of coil states to the slave.
     * <p/>
     * Note that the number of coils to be written is given
     * implicitly, through {@link BitVector#size()}.
     *
     * @param ref   the offset of the coil to start writing to.
     * @param coils a <tt>BitVector</tt> which holds the coil states to be written.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public synchronized void writeMultipleCoils(int ref, BitVector coils) throws ModbusException {
        writeMultipleCoils(DEFAULT_UNIT_ID, ref, coils);
    }

    /**
     * Reads a given number of input discrete states from the slave.
     * <p/>
     * Note that the number of bits in the bit vector will be
     * forced to the number originally requested.
     *
     * @param ref   the offset of the input discrete to start reading from.
     * @param count the number of input discrete states to be read.
     *
     * @return a <tt>BitVector</tt> instance holding the received input discrete
     * states.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public synchronized BitVector readInputDiscretes(int ref, int count) throws ModbusException {
        return readInputDiscretes(DEFAULT_UNIT_ID, ref, count);
    }

    /**
     * Reads a given number of input registers from the slave.
     * <p/>
     * Note that the number of input registers returned (i.e. array length)
     * will be according to the number received in the slave response.
     *
     * @param ref   the offset of the input register to start reading from.
     * @param count the number of input registers to be read.
     *
     * @return a <tt>InputRegister[]</tt> with the received input registers.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public synchronized InputRegister[] readInputRegisters(int ref, int count) throws ModbusException {
        return readInputRegisters(DEFAULT_UNIT_ID, ref, count);
    }

    /**
     * Reads a given number of registers from the slave.
     * <p/>
     * Note that the number of registers returned (i.e. array length)
     * will be according to the number received in the slave response.
     *
     * @param ref   the offset of the register to start reading from.
     * @param count the number of registers to be read.
     *
     * @return a <tt>Register[]</tt> holding the received registers.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public synchronized Register[] readMultipleRegisters(int ref, int count) throws ModbusException {
        return readMultipleRegisters(DEFAULT_UNIT_ID, ref, count);
    }

    /**
     * Writes a single register to the slave.
     *
     * @param ref      the offset of the register to be written.
     * @param register a <tt>Register</tt> holding the value of the register
     *                 to be written.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public synchronized void writeSingleRegister(int ref, Register register) throws ModbusException {
        writeSingleRegister(DEFAULT_UNIT_ID, ref, register);
    }

    /**
     * Writes a number of registers to the slave.
     *
     * @param ref       the offset of the register to start writing to.
     * @param registers a <tt>Register[]</tt> holding the values of
     *                  the registers to be written.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public synchronized void writeMultipleRegisters(int ref, Register[] registers) throws ModbusException {
        writeMultipleRegisters(DEFAULT_UNIT_ID, ref, registers);
    }

}