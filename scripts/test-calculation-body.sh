#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
test_classes=$(mktemp -d)
trap 'rm -rf "$test_classes"' EXIT
java_runtime=${JAVA_HOME:+$JAVA_HOME/bin/}java
source_dir=app/src/main/java/ir/kamranvahdati/lawoffice
"$java_runtime" com.sun.tools.javac.Main -encoding UTF-8 --release 8 -d "$test_classes" \
 "$source_dir/JalaliDate.java" "$source_dir/CalculationArithmetic.java" \
 "$source_dir/CalculationReference.java" "$source_dir/CalculationBodyMath.java" "$source_dir/CalculationDeath.java" "$source_dir/CalculationPrescribedBody.java" \
 app/src/test/java/ir/kamranvahdati/lawoffice/CalculationDeathTest.java app/src/test/java/ir/kamranvahdati/lawoffice/CalculationBodyTest.java app/src/test/java/ir/kamranvahdati/lawoffice/CalculationPrescribedBodyTest.java
"$java_runtime" -cp "$test_classes" ir.kamranvahdati.lawoffice.CalculationBodyTest app/src/main/assets/calculation/diyah-1405.properties

"$java_runtime" -cp "$test_classes" ir.kamranvahdati.lawoffice.CalculationPrescribedBodyTest app/src/main/assets/calculation/prescribed-head-1392-v1.properties app/src/main/assets/calculation/diyah-1405.properties

"$java_runtime" -cp "$test_classes" ir.kamranvahdati.lawoffice.CalculationDeathTest app/src/main/assets/calculation/death-1392-v1.properties app/src/main/assets/calculation/diyah-1405.properties
