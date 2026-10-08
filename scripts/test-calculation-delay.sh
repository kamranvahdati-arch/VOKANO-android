#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
test_classes=$(mktemp -d)
trap 'rm -rf "$test_classes"' EXIT
java_compiler=${JAVA_HOME:+$JAVA_HOME/bin/}javac
java_runtime=${JAVA_HOME:+$JAVA_HOME/bin/}java
source_dir=app/src/main/java/ir/kamranvahdati/lawoffice
"$java_runtime" com.sun.tools.javac.Main -encoding UTF-8 --release 8 -d "$test_classes" \
  "$source_dir/JalaliDate.java" "$source_dir/CalculationArithmetic.java" \
  "$source_dir/CalculationReference.java" "$source_dir/CalculationDelayMath.java" "$source_dir/CalculationPartialPayments.java" "$source_dir/CalculationDelayExceptions.java" "$source_dir/CalculationDelayPack.java" "$source_dir/CalculationCurrentIndices.java" \
  app/src/test/java/ir/kamranvahdati/lawoffice/CalculationCurrentIndicesTest.java app/src/test/java/ir/kamranvahdati/lawoffice/CalculationDelaySmokeTest.java \
  app/src/test/java/ir/kamranvahdati/lawoffice/CalculationDelayPackTest.java app/src/test/java/ir/kamranvahdati/lawoffice/CalculationPartialPaymentsTest.java app/src/test/java/ir/kamranvahdati/lawoffice/CalculationDelayExceptionsTest.java
"$java_runtime" -cp "$test_classes" ir.kamranvahdati.lawoffice.CalculationDelaySmokeTest

"$java_runtime" -cp "$test_classes" ir.kamranvahdati.lawoffice.CalculationDelayPackTest

"$java_runtime" -cp "$test_classes" ir.kamranvahdati.lawoffice.CalculationCurrentIndicesTest

"$java_runtime" -cp "$test_classes" ir.kamranvahdati.lawoffice.CalculationPartialPaymentsTest

"$java_runtime" -cp "$test_classes" ir.kamranvahdati.lawoffice.CalculationDelayExceptionsTest
