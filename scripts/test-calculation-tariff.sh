#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
test_classes=$(mktemp -d)
trap 'rm -rf "$test_classes"' EXIT
java_runtime=${JAVA_HOME:+$JAVA_HOME/bin/}java
source_dir=app/src/main/java/ir/kamranvahdati/lawoffice
"$java_runtime" com.sun.tools.javac.Main -encoding UTF-8 --release 8 -d "$test_classes" \
  "$source_dir/JalaliDate.java" "$source_dir/CalculationArithmetic.java" \
  "$source_dir/CalculationReference.java" "$source_dir/CalculationTariff.java" \
  app/src/test/java/ir/kamranvahdati/lawoffice/CalculationTariffGoldenTest.java
"$java_runtime" -cp "$test_classes" ir.kamranvahdati.lawoffice.CalculationTariffGoldenTest \
  app/src/main/assets/calculation/tariff-1398-reviewed.properties
