JKL
=====

NAME
-----

JKL - It is JMX command line client tool.


SYNOPSIS
-----

### FEATURES
- Ping to JMX server.
- List up MBeans registered on JMX Server.
- List up Attributes of MBean.
- Retrieve Value of MBean metric or some information.
- Support csv, tsv and list format output.


### USAGE
#### With [jar](https://github.com/digitalbot/jkl/releases/download/1.0.0/jkl-1.0.0-all.jar)
```
$ java -jar jkl-1.0.0-all.jar [OPTIONS] HOST:PORT [BEAN] [ATTRIBUTE] [TYPE]
```
#### With [zip](https://github.com/digitalbot/jkl/releases/download/1.0.0/jkl-1.0.0-zip)
```
$ jkl [OPTIONS] HOST:PORT [BEAN] [ATTRIBUTE] [TYPE]
```

### INSTALLATION
#### With java command
Go to [releases page](https://github.com/digitalbot/jkl/releases/), find the version you want, and download **jar** file, and you only specify it using java's -jar option.

#### Like executable script
Go to [releases page](https://github.com/digitalbot/jkl/releases/), find the version you want, and download **zip** file. Unpack the zip file, and you execute unpacked command file.

#### Single binary
NOT IMPLEMENTED...


### REQUIREMENTS
- JVM Target >= 1.8


OPTIONS
-----

### -v, --version
Display a version of jkl.

### -h, --help
Display a help message.

### -p, --ping
Ping to your JMX server. If a connection is alive, this option is do nothiong and display no messages. If not, display error messages.

### -t, --target VALUE
Use VALUE as target. This can be used to specify multiple search retrieve values. When this option specified, jkl just print empty string and do not throw error messages if invalid bean names, attribute names and attribute types specified. This option cannot specify with BEAN, ATTRIBUTE and TYPE arguments.

Format: `BEAN\tATTRIBUTE[\tTYPE][\tALIAS]`

Parameters TYPE and ALIAS are optional. ALIAS is using for `--show-keys` option.

### -f, --file FILE
Use FILE contens as target. See more `--target` option.

### --show-keys
Display keys. 

Format: `BEAN::ATTRIBUTE[::TYPE]` (or `ALIAS` if you specified.)


### --use-tab
Change a separator from COMMA to TAB character. (And disable double quote for comma).
This option for `--output=csv`

### -o, --output [csv|list]
Change a output format.
List format does not double quote.


EXAMPLE
-----

### List up all MBeans
- csv format
```
$ jkl localhost:10048
JMImplementation:type=MBeanServerDelegate,com.sun.management:type=DiagnosticCommand,com.sun.management:type=HotSpotDiagnostic,"java.lang:name=CodeCacheManager,type=MemoryManager","java.lang:name=CodeHeap 'non-nmethods',type=MemoryPool","java.lang:name=CodeHeap 'non-profiled nmethods',type=MemoryPool","java.lang:name=CodeHeap 'profiled nmethods',type=MemoryPool","java.lang:name=Compressed Class Space,type=MemoryPool","java.lang:name=G1 Eden Space,type=MemoryPool","java.lang:name=G1 Old Gen,type=MemoryPool","java.lang:name=G1 Old Generation,type=GarbageCollector","java.lang:name=G1 Survivor Space,type=MemoryPool","java.lang:name=G1 Young Generation,type=GarbageCollector","java.lang:name=Metaspace Manager,type=MemoryManager","java.lang:name=Metaspace,type=MemoryPool",java.lang:type=ClassLoading,java.lang:type=Compilation,java.lang:type=Memory,java.lang:type=OperatingSystem,java.lang:type=Runtime,java.lang:type=Threading,"java.nio:name=direct,type=BufferPool","java.nio:name=mapped,type=BufferPool",java.util.logging:type=Logging,jdk.management.jfr:type=FlightRecorder
```

- list format
```
$ jkl localhost:10048 -o list
JMImplementation:type=MBeanServerDelegate
com.sun.management:type=DiagnosticCommand
com.sun.management:type=HotSpotDiagnostic
java.lang:name=CodeCacheManager,type=MemoryManager
java.lang:name=CodeHeap 'non-nmethods',type=MemoryPool
java.lang:name=CodeHeap 'non-profiled nmethods',type=MemoryPool
java.lang:name=CodeHeap 'profiled nmethods',type=MemoryPool
java.lang:name=Compressed Class Space,type=MemoryPool
java.lang:name=G1 Eden Space,type=MemoryPool
java.lang:name=G1 Old Gen,type=MemoryPool
java.lang:name=G1 Old Generation,type=GarbageCollector
java.lang:name=G1 Survivor Space,type=MemoryPool
java.lang:name=G1 Young Generation,type=GarbageCollector
java.lang:name=Metaspace Manager,type=MemoryManager
java.lang:name=Metaspace,type=MemoryPool
java.lang:type=ClassLoading
java.lang:type=Compilation
java.lang:type=Memory
java.lang:type=OperatingSystem
java.lang:type=Runtime
java.lang:type=Threading
java.nio:name=direct,type=BufferPool
java.nio:name=mapped,type=BufferPool
java.util.logging:type=Logging
jdk.management.jfr:type=FlightRecorder
```

### List up attribute
```
$ jkl localhost:10048 -o list -- "java.lang:type=Memory"
ObjectPendingFinalizationCount
HeapMemoryUsage
NonHeapMemoryUsage
Verbose
ObjectName
```

### Retrieve attribute values
```
$ jkl localhost:10048 -- "java.lang:type=Memory" HeapMemoryUsage
268435456,268435456,4294967296,13927800
```

### Retrieve attribute values with keys header
```
$ jkl localhost:10048 --show-keys -- "java.lang:type=Memory" HeapMemoryUsage
java.lang:type=Memory::HeapMemoryUsage::committed,java.lang:type=Memory::HeapMemoryUsage::init,java.lang:type=Memory::HeapMemoryUsage::max,java.lang:type=Memory::HeapMemoryUsage::used
268435456,268435456,4294967296,15017688
```

### Retrieve type filtered attribute value 
```
$ jkl localhost:10048 --show-keys -- "java.lang:type=Memory" HeapMemoryUsage init
java.lang:type=Memory::HeapMemoryUsage::init
268435456
```

### Retrieve values using '--target' option with aliased key header 
```
$ jkl localhost:10048 --show-keys --output=list "-t=foo\tbar\tbaz\talias" "-t=java.lang:type=Memory\tHeapMemoryUsage\tmax\tHeapMemoryUsageMax"
alias	
HeapMemoryUsageMax	4294967296
```

### Retrieve values using '--file' option
```
$ echo "java.lang:name=G1 Old Generation,type=GarbageCollector\tCollectionCount\t\tG1OldGCCount" > target.tsv
$ jkl localhost:10048 --show-keys --file=target.tsv
G1OldGCCount
0
```


TODO
-----

- more test...
- GRAALVM


NOTES
-----

This tool is inspired by other tools or documents below.
- [jmx-cmdclient](https://github.com/uzresk/jmx-cmdclient)
- [cmdline-jmxclient](http://crawler.archive.org/cmdline-jmxclient/)
- [Creating a Custom JMX Client](https://docs.oracle.com/javase/tutorial/jmx/remote/custom.html)
- ...


LICENSE
-----
Copyright (C) digitalbot
This software is released under the MIT License, see LICENSE.txt.
