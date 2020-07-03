#!/bin/bash

mvn package

rm -rf python-wrapper/build
rm -rf python-wrapper/dist
rm -rf python-wrapper/orekit.egg-info

pip uninstall orekit


export SRC_DIR=${PWD}

python -m jcc \
--use_full_names \
--python orekit \
--version 10.1 \
--jar $SRC_DIR/orekit-10.1.jar \
--jar $SRC_DIR/hipparchus-core-1.6.jar \
--jar $SRC_DIR/hipparchus-filtering-1.6.jar \
--jar $SRC_DIR/hipparchus-fitting-1.6.jar \
--jar $SRC_DIR/hipparchus-geometry-1.6.jar \
--jar $SRC_DIR/hipparchus-ode-1.6.jar \
--jar $SRC_DIR/hipparchus-optim-1.6.jar \
--jar $SRC_DIR/hipparchus-stat-1.6.jar \
--package java.io \
--package java.util \
--package java.text \
--package org.orekit \
--package org.orekit.rugged \
java.io.BufferedReader \
java.io.FileInputStream \
java.io.FileOutputStream \
java.io.InputStream \
java.io.InputStreamReader \
java.io.ObjectInputStream \
java.io.ObjectOutputStream \
java.io.PrintStream \
java.io.StringReader \
java.io.StringWriter \
java.lang.System \
java.text.DecimalFormat \
java.text.DecimalFormatSymbols \
java.util.ArrayDeque  \
java.util.ArrayList \
java.util.Arrays \
java.util.Collection \
java.util.Collections \
java.util.Date \
java.util.HashMap \
java.util.HashSet \
java.util.List \
java.util.Locale \
java.util.Map \
java.util.Set \
java.util.TreeSet \
java.util.stream.Collectors \
java.util.stream.Stream \
java.util.stream.DoubleStream \
--module $SRC_DIR/pyhelpers.py \
--reserved INFINITE \
--reserved ERROR \
--reserved OVERFLOW \
--reserved NO_DATA \
--reserved NAN \
--reserved min \
--reserved max \
--reserved mean \
--reserved SNAN \
--classpath /home/fergus/src/Anaconda/anaconda3/lib/tools.jar \
--files 81 \
--build \
--install
