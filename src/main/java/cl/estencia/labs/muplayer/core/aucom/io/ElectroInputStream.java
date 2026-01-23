package cl.estencia.labs.muplayer.core.aucom.io; ///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package cl.estencia.labs.aucom.io;
//
//import org.bytebuffer.ByteBuffer;
//
//import java.io.IOException;
//import java.io.InputStream;
//
//import static cl.estencia.labs.muplayer.core.aucom.common.IOConstants.EOF;
//
///**
// *
// * @author martin
// */
//
//public class ElectroInputStream extends InputStream{
//
//    private final ByteBuffer buffer;
//    private int pos;
//    private int readLimit;
//    private boolean isMarked;
//
//    public ElectroInputStream() {
//        buffer = new ByteBuffer(10240);
//        pos = 0;
//        readLimit = 0;
//        isMarked = false;
//    }
//
//    public ByteBuffer getListBytes() {
//        return buffer;
//    }
//
//    void addBytes(byte b){
//        buffer.add(b);
//    }
//
//    public void clear(){
//        buffer.clear();
//    }
//
////    public byte[] readBytes(){
////        return readBytes(1024);
////    }
////
////    public byte[] readBytes(int len){
////        int listSize = listBytes.size();
////        byte[] buff = new byte[len > listSize ? listSize : len];
////        int count = 0;
////        int i = 0;
////        for (Byte b : listBytes){
////            if (count >= regexRead)
////                buff[i++] = b;
////            count++;
////            if (count == buff.length)
////                break;
////        }
////        regexRead+=buff.length;
////        if (regexRead >=listBytes.size())
////            regexRead =  0;
////        if (listBytes.size() >= 1024*1024) {
////            listBytes.clear();
////            regexRead = 0;
////        }
////        return buff;
////    }
//
//    @Override
//    public int available() throws IOException {
//        return buffer.size()-pos;
//    }
//
//    public boolean hasBytes(){
//        return !buffer.isEmpty();
//    }
//
//    @Override
//    public int read(byte[] b, int off, int len) throws IOException {
//        //System.out.println("read largo");
//        System.out.println("Pos: "+pos);
//        if (!hasBytes())
//            return EOF;
//        if (len > b.length)
//            len = b.length;
//        if (len > available())
//            len = available();
//
//        int readLen = (isMarked && readLimit < len) ? readLimit : len;
//        System.out.println("ReadLen: "+readLen);
//        int read = buffer.read(b, off, readLen);
//        if (read == -2)
//            pos = 0;
//        buffer.removeAt(0, len);
//        //pos += read;
//        return len;
//    }
//
//    @Override
//    public int read(byte[] b) throws IOException {
//        //System.out.println("read medio");
//        int read = read(b, 0, b.length);
//        return read;
//    }
//
//    @Override
//    public int read() throws IOException {
//        //System.out.println("read");
//        if (isMarked && readLimit == 0)
//            return EOF;
//        //pos++;
//        return buffer.isEmpty() ? EOF : buffer.pollFirst();
//    }
//
//    public byte[] read(int len) throws IOException{
//        if (available() == 0)
//            return null;
//        if (len > available())
//            len = available();
//        byte[] b = new byte[len];
//        read(b);
//        skip(len);
//        return b;
//    }
//
//    public byte[] readAllBytes(){
//        return buffer.toArray();
//    }
//
//    @Override
//    public boolean markSupported() {
//        return true;
//    }
//
//    @Override
//    public synchronized void reset() throws IOException {
//        pos = 0;
//    }
//
//    @Override
//    public synchronized void mark(int readlimit) {
//        this.readLimit = readlimit;
//        isMarked = true;
//    }
//
//    @Override
//    public void close() throws IOException {
//        pos = buffer.size();
//        readLimit = 0;
//    }
//
//    @Override
//    public long skip(long n) throws IOException {
//        pos+=n;
//        return n;
//    }
//
//
//
//}
