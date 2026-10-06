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

Official-source verification is partly completed, not a released rule pack.
On 2026-10-06 the full 34-article tariff was read at official Qavanin record
`16814137751518820692`, its consolidation relationships inspected, and the
Central Bar republication cross-checked. See `V10_3_LEGAL_SOURCE_REVIEW.md` for
URLs, every article's computational relevance, unresolved interactions and
independently derived (not executed) tariff fixture candidates. Do not repeat
this extraction. The retrieved relationships page lists no affecting instrument;
that observation is not a guarantee against every later legal change.
Official verification of penal provisions and rulings 850/812 remains unfinished.
Official parliament URL `https://rc.majlis.ir/fa/law/show/845048` previously
returned 502. Do not silently substitute commercial calculators. The owner's
21,000,000,000-rial example remains unverified annual reference data.

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
- Remote implementation checkpoint: `f21a8b0ddd8253088a4d0abf06d3d085d4b8e1ce`.
  Tree `3aa6f59acd475bde60f1d197c482098c56b437be` exactly matches the original local
  checkpoint `86affeeb8bb2f9f9337d7358b79e8c4fb48c2333`, preserved on local checkpoint
  branches. Git push lacked credentials; the authenticated GitHub connector
  preserved the identical tree. Current work branch tracks remote `codex/v10.3`.
- CI run `37377551905`, job `111990588671`, on the implementation checkpoint:
  completed SUCCESS. Foundation assertions and Android release/test APK builds
  succeeded. Test-only artifact `11372472107` was produced. No production signing
  and no emulator execution were performed in this new workflow.
- No existing application source has been edited. No UI entry, database/schema,
  preferences, signing, backup format, navigation or version number changed.

## First incomplete work and remaining gates

1. Preserve successful baseline and foundation tests; do not repeat them absent
   a relevant code change. Documentation-only evidence commits do not require
   another build of the identical application source.
2. FIRST INCOMPLETE GATE: Finish authoritative legal-source verification:
   penal diyah/arsh/taghliz provisions, article 522 and rulings 850/812; review
   applicable changes. Tariff's full official text and article extraction are
   now saved; resolve flagged rule interactions before enabling those branches.
   Retrieval failures/incomplete coverage still affect the other sources.
   Do not mark unofficial reproductions as an officially verified rule pack.
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

## Continuation checkpoint — 2026-10-06 UTC

- Resumed remote/local `codex/v10.3` at
  `3868c6f732f8099d0c860275e8c8144fe7d03aaa`, clean worktree before edits.
- Maintenance had removed the old scratch clone; fresh clone restored the
  existing branch/history, not a restart or reset of development.
- Read the two supplied project/permanent execution-rule attachments.
- Changed only this checkpoint and `docs/V10_3_LEGAL_SOURCE_REVIEW.md`.
- No migration created/applied; schema 15; application 10.2 / code 15 unchanged.
- No new build/test/emulator run: this increment changes documentation only;
  previous successful baseline/foundation CI results above remain historical
  evidence, not new passes. Legal goldens, new backup/restore and installed
  10.2→10.3 upgrade remain unperformed. No new APK produced.
- First next task is the remaining legal-source gate above, not a full audit or
  rerun of successful tests for unchanged application code.
