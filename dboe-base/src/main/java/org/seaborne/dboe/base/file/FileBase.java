/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.base.file;

import java.io.IOException ;
import java.nio.channels.FileChannel ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sync ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public final class FileBase implements Sync, Closeable
{
    static private Logger log = LoggerFactory.getLogger(FileBase.class) ; 
    // A mixin, which java does not support very well.
    // Better done as a library e.g. size(FileChannel) => ...
    public final String filename ;
    private FileChannel channel ;
    public static boolean DEBUG = false ;
    private final boolean DebugThis  ;
    private static long counter = 0 ;
    private final long id ;

    /** Create an FileBase without managed resources.  Use with care. */
    static FileBase xcreateUnmanged(String filename, FileChannel channel) { return new FileBase(filename, channel) ; }
    
    /** Create a Filebase with managed resources */
    static FileBase xcreate(String filename) { return new FileBase(filename) ; }
    static FileBase xcreate(String filename, String mode) { return new FileBase(filename, mode) ; }
    
    private /*public*/ FileBase(String filename)
    {
        this(filename, "rw") ;
    }
    
    private /*public*/ FileBase(String filename, String mode)
    {
        DebugThis = false ;
        id  = (counter++) ;
        
        if ( DebugThis && log.isDebugEnabled() )
            log.debug("open: ["+id+"]"+filename) ;
        this.filename = filename ;
        channel = ChannelManager.acquire(filename, mode) ;
    }
    
    private /*public*/ FileBase(String filename, FileChannel channel)
    {
        DebugThis = false ;
        id  = -1  ;
        this.filename = filename ;
        this.channel = channel ; 
    }

    public final FileChannel channel() { return channel ; }
    
    public long size() {
        try {
            return channel.size() ;
        } catch (IOException ex)
        { IO.exception(ex) ; return -1L ; }
    }

    public void truncate(long posn) {
        if ( DebugThis )
            log.debug("truncate: ["+id+"]: "+filename) ;
        try { channel.truncate(posn) ; }
        catch (IOException ex) { IO.exception(ex) ; }
    }


    public boolean isClosed() {
        return channel == null ;
    }
    
    @Override
    public void close() {
        if ( DebugThis )
            log.debug("close: ["+id+"]: "+filename) ;
        ChannelManager.release(channel) ;
        channel = null ;
//        try {
//            channel.close() ;
//            channel = null ;
//        } catch (IOException ex)
//        { throw new FileException("FileBase.close", ex) ; }
    }

    @Override
    public void sync() {
        if ( DebugThis ) 
            log.debug("sync: ["+id+"]: "+filename) ;
        try {
            channel.force(false) ;
        } catch (IOException ex)
        { throw new FileException("FileBase.sync", ex) ; }
    }

    public String getFilename() { return filename ; }  
}
