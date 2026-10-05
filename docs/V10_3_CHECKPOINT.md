# VOKANO 10.3 — Legal Calculation Center

Status: IN PROGRESS, NOT A RELEASE. Version metadata remains 10.2 / 15.

## Verified starting point (2026-10-05 UTC)

- Repository: `kamranvahdati-arch/VOKANO-android` (public).
- Both authenticated GitHub commit lookup and `git ls-remote` resolve
  `codex/v10.2` to `da5ec5fd4be843851b3f0b7229bff9e85627cb46`.
- Starting tree: `09b3255573fb39e1fe76e66f1e847c540d2e75c8`.
- Fresh clone was clean; work branch: `codex/v10.3`.
- Main is not the baseline. The older local working copy was left untouched.
- Application ID: `ir.kamranvahdati.lawoffice`; versionName 10.2;
  versionCode 15; OfficeDb schema 15. Signing configuration unchanged.
- Migration chain through schema 15 inspected, including OfficeV101 migration.
- Existing backup explicitly enumerates tables and validates completeness.
  A future schema-16 addition must preserve acceptance of valid schema-15
  backups while rejecting incomplete new-format calculation backups.
- Existing CI run 37136067053, attempt 1: all seven jobs succeeded at this HEAD
  (account contract; API 30/35 clean-reinstall; API 30/35 V10 and V10.1 upgrade).
- Requested fresh execution by rerunning job 111240732506, creating attempt 2.
  Attempt 2 subsequently completed successfully; verified directly through
  GitHub run endpoint before this checkpoint. This is 10.2 baseline evidence,
  not a 10.3 migration/upgrade or final release result.
- All four existing pure-Java smoke suites compiled and passed locally:
  DeadlineDateSmokeTest, InputValidatorsSmokeTest, BackupCipherSmokeTest,
  ProfessionalAccountSmokeTest.
- Local Android build was attempted with JDK 17 and Gradle 8.9. It failed while
  resolving Android Gradle Plugin 8.7.3. SDK manager could not fetch manifests.
  CI remains the available Android build runner; local build is NOT a pass.

## Binding scope

The owner's 32-part Version 10.3 instruction governs this work. Add only the
Legal Calculation Center to healthy 10.2; no general refactor or redesign.

1. Standalone center and a Calculations entry inside each case; read-only
   suggestions from existing case/contracts; no automatic case modifications.
2. Separate Android-independent calculation engine, versioned rules and rates;
   integer/BigDecimal arithmetic, explicit rial/toman presentation, validated
   Persian/Arabic/Latin digits, no intermediate rounding or silent overflow.
3. Four domains: agreed/tariff lawyer fees; ordinary-debt/cheque delay damages;
   prescribed diyah; explicitly non-final arsh assistance.
4. Tariff rules require full authoritative 1398/12/28 regulation review and
   amendment verification BEFORE formula implementation; no generic percentage
   substituted for financial/nonfinancial/criminal/stage/service-specific rules.
5. Diyah: versioned yearly base (owner's 1405 figure is only a validation
   example), supported injury categories/fractions/independent totals; explicit
   effective-date choice; no previous-year rate fallback. Taghliz must consider
   death versus injury, act/death timing and Mecca sanctuary conditions.
6. Arsh warning must survive screen, PDF, print and share. Expert opinions,
   proposed percentages and related awards remain distinguishable.
7. Delay damage modes remain distinct; validate dates, demand/ability/refusal
   information as relevant, same-series official annual indexes published by
   month, no guessed index. Preserve adjusted total and damages separately.
8. Versioned reference records include ID/type/year/effective interval/value/unit,
   source title/type/number/date/URL/status/notes, timestamps, confirmation,
   manual entry and supersession. Index data additionally has month and series.
9. Immutable input/result/reference snapshots include engine/legal/rate versions,
   calculation and effective dates, rounding rule, override state and timestamps.
10. Preserve previous rate/index/rule versions and every override's before/after,
    timestamp, type and explanation. Recalculation/duplication creates a new
    calculation snapshot. Save/name/reopen/link independent calculations.
11. Minimal additive database tables only; nullable case relation; no existing
    data reset/reseed/rebuild. Include all new tables in existing backup/restore.
12. Persian RTL forms use existing theme and components; four center cards;
    readable grouped amounts with explicit currency; no branding/navigation
    redesign. Outputs explain inputs, basis, steps, sources and limitations.
13. Baseline build/tests before implementation; engine edge/boundary/missing/
    override/historical/rounding/large-value tests; independently derived legal
    golden fixtures; existing regression; installed 10.2→10.3 preservation;
    new backup/restore; API 30 and 35; permanent signing identity retained.
14. Bump to 10.3 and next versionCode only after migration/regression succeed.
    Release completion requires actual passing evidence and a final commit.
15. No cloud/sync/iOS/Windows/web/team/AI/OCR/research/platform/licensing/referral
    changes or unrelated accounting/case/person features.

## Legal evidence gate

Official-source verification is unfinished. Search found unofficial reproductions
of the tariff, ruling 850 and penal provisions, which are NOT a verified rule
pack. Official parliament law URL `https://rc.majlis.ir/fa/law/show/845048`
returned 502 through retrieval. Do not invent source verification or silently use
commercial calculators. Rate 21,000,000,000 rials supplied by owner is a test
example, not an independently verified official 1405 data record.

## Resume rule

Verify remote/local branch, HEAD, worktree and this checkpoint. Preserve all
valid commits and work. Continue the first unfinished gate, not a new audit.
Before any forced stop, record actual changed files/tests/migrations/build state
and preserve valid work in a checkpoint commit on the repository.

## Implemented technical foundation (not yet wired into the app)

- `CalculationArithmetic`: strict Persian/Arabic/Latin digit parsing, validated
  grouping and decimals, explicit rial/toman conversion, long overflow rejection,
  percentages, exact rational arithmetic and explicit final rounding. No law,
  tariff, index or yearly rate is embedded.
- `CalculationReference`: immutable version/source/effective-interval metadata;
  positive yearly rates and indexes; manual confirmation; no previous-year or
  different-series fallback; ambiguous active versions require explicit choice.
- `CalculationSnapshot`: immutable copies of input/output/reference data,
  engine/legal/data versions, dates, nullable case identity, rounding and audit
  history. Mandatory arsh estimate warning is preserved in the model. This does
  NOT claim PDF/print/share support, which is still absent.
- `CalculationFoundationSmokeTest`: 65 technical assertions passed using JDK 17
  with Java 8 target. These are synthetic mathematical/validation fixtures,
  explicitly NOT independently verified legal golden fixtures.
- `scripts/test-calculation-foundation.sh` reproduces those tests without Android.
- `.github/workflows/v103.yml` builds this development checkpoint with test signing
  and runs foundation assertions. Its output is not a production update APK.
- No existing application source has been edited. No UI entry, database/schema,
  preferences, signing, backup format, navigation or version number changed.

## First incomplete work and remaining gates

1. Check the new checkpoint CI build result. Preserve successful baseline tests;
   do not repeat them absent a relevant code change.
2. Retrieve and verify authoritative full legal sources and applicable amendments:
   tariff 1398/12/28 (all effective articles); penal diyah/arsh/taghliz provisions;
   article 522 and rulings 850/812. Official legal verification remains blocked
   by retrieval failures/incomplete search coverage. Do not mark unofficial
   reproductions as an officially verified rule pack.
3. Implement actual domain engines and verified rule/reference packs, including
   fee stages/services and exceptions, separate delay modes, prescribed injuries,
   taghliz eligibility and arsh assistance. Current code is infrastructure only.
4. Add minimal schema migration and persistence, revision/audit repositories,
   nullable case link, compatibility with existing backup/restore, UI forms and
   history, duplicate/recalculate, PDF/print/share preserving warnings.
5. Legal golden tests, migration preservation, 10.2→10.3 installed upgrade,
   calculation backup/restore and API 30/35 regression are NOT performed yet.
6. After gates succeed, bump versionName/versionCode, create production-signed
   release using existing identity and verify it; final release commit pending.

Migration state: no new migration created or applied; schema remains 15.
Release state: 10.3 incomplete, no production 10.3 APK. All subsequent reports
must distinguish baseline CI, technical foundation checks and release criteria.
