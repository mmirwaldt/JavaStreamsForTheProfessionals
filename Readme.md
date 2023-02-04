# Java Streams for the professionals

### What is this?

This project includes the slides and the code examples of the presentations "Mit dem Strom schwimmen statt gegen ihn. -
Java Streams für Profis" (German) and "Peril streams?! Oh, you mean parallel streams! Java Streams for the
professionals".

## Where can you find the slides?

You can find them here:

* in German: https://github.com/mmirwaldt/JavaStreamsForTheProfessionals/blob/master/JavaStreamsFuerProfis.pdf
* in English: https://github.com/mmirwaldt/JavaStreamsForTheProfessionals/blob/master/JavaStreamsForProfessionals.pdf

They are protected by a password which you get when you watch this talk.

## Which Java version is needed for examples?

You need Java 19 to run the examples because they use BigInteger.parallelMultiply which was added with Java 19.

## What examples can you find here?

Two kinds of examples exist:

* benchmarks follow the name pattern "Benchmark_XY_Name". Those are JMH benchmarks. 
You don't need to run them to see results because you can find them in a comment in those files.
* examples follow the name pattern "ParallelStream_XY_Name". Those are simple examples which show something interesting.

### Who owns the copyright for this project?

Michael Mirwaldt owns the copyright (c) for this project since 2022. All rights reserved to him.

### How is this project licensed?

<a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc-nd/4.0/88x31.png" /></a><br />
This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/">Creative Commons
Attribution-NonCommercial-NoDerivatives 4.0 International License</a>.