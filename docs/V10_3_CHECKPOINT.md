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

## Numerical delay increment — 2026-10-06

Starting HEAD: e965f8cbad3bfd1dd6556727456137d371a7c1f7.
Added CalculationDelayMath, CalculationDelaySmokeTest, its test script and a CI
step; updated source evidence above. No existing application behavior is wired
to this class; no database, backup, migration, version or signing changes.

Ruling 850 official text is now retrieved; other legal gates remain open as
detailed in V10_3_LEGAL_SOURCE_REVIEW.md. Distinct modes preserve caller context;
legal qualification and date selection are NOT implemented. Do not expose this
helper as an automatic entitlement calculator before those gates succeed.

Local new-suite result: 26 assertions PASS with Java 17, --release 8. The javac
launcher was absent; java com.sun.tools.javac.Main successfully compiled the
suite using the installed jdk.compiler module. No package installation succeeded.
CI includes the new suite; its actual result must be checked, not presumed.
No local Android build,
emulator, migration, backup/restore or legal eligibility golden pass is claimed.
Version remains 10.2 / 15; schema 15. Next: verify new CI result, finish remaining
legal assessment sources/rules, then continue persistence and UI.

Verified CI outcome for numerical increment: run 37438853979 on commit
68e82d1e061db7e8bff79d24f91634a154e8d698 completed SUCCESS. Foundation and
new delay tests, Android release and release-test APK compilation passed.
This workflow uses TEST signing; it is not a production 10.3 release, and
has no emulator/upgrade/backup-restore execution. Documentation-only recording
of this result does not require rebuilding identical application sources.

## Active continuation — tariff and calculation storage, 2026-10-06

Starting HEAD verified directly through GitHub: f8797581f46eb19886203d62af70c71c267958b5.
Local clean branch was fast-forwarded to it without resetting any work.

- New CalculationTariff engine and separate reviewed properties pack; 68 local
  assertions passed, including the source-derived progressive tariff goldens.
- Signed ruling 812 image read; see legal review. Remaining official statute and
  later-exception review is still blocked by retrieval failures.
- New CalculationJson lossless format and OfficeCalculations insert-only revision
  repository. Two additive schema-16 tables, nullable case linkage; no rewrite of
  existing migrations or existing office rows.
- Backup export now records schema/calculation format. Valid old backups without
  calculation tables remain accepted. New-format incomplete tables, invalid
  payloads and broken revision/reference chains must fail transactionally.
- New Android storage/backup tests and installed immutable 10.2-to-schema16
  fixtures, plus API30/35 CI jobs. At checkpoint creation they are NOT yet run;
  compile/emulator outcomes must be verified before any passing claim.
- versionName/code still 10.2/15; schema candidate is 16. No release signing or
  publication occurred. No calculation UI is connected yet.

Next: inspect CI compilation and API30/35 storage/upgrade results; fix any real
failure, then wire supported calculation forms/history/export. Remaining legal
gates prevent declaring a complete 10.3 even if technical tests pass. Preserve
old successful arithmetic tests; new CI also runs them because new domain/storage
integration is now changing the candidate. Do not stop merely at this checkpoint.

### Verified storage checkpoint and next UI increment

Remote checkpoint `70a63145d934f0a542a94a9fc8362441a4e39536` has the identical tree
`a8a8a8c4393cee4d44e8df56f5b01ee858f25fd2` as preserved local commit 58179e2.
Git push lacked credentials; GitHub connector publication succeeded.
CI run `37489343679`: all three jobs SUCCESS. The pure-Java foundation/delay/
tariff suites and Android release/test compilation passed. Both API30 and API35
installed immutable schema-15 baseline, seeded office data, installed candidate
without uninstall/clear, and verified schema-16 preservation. The tests then
passed calculation encrypted backup round-trip, malformed/incomplete restore
rollback, legacy backup acceptance, and scoped 10.2 regression. This used TEST
signing and development versionName 10.2, NOT a production 10.3 release.

The next increment adds the center/case entry, agreed/tariff forms, immutable
save/history/revision, and a single report text for display/PDF/share. Historical
tariff pack content is captured in the input snapshot. Other domain cards are
explicitly unavailable until their legal gate is complete; no automatic legal
eligibility claim is exposed. A new light/dark UI save/revision test is added.
Its new build/UI results are pending at this checkpoint; do not substitute the
preceding storage-run success for this changed candidate.

## Reference-entry continuation — 2026-10-07

Verified remote HEAD cb43d9d291b76effe752e498495a68ba20f43f1b; local 3959284
has identical tree fa8b6bad48b463d346dca8a3250b253cc4c14d6f and is preserved
on checkpoint/local-3959284. Continued on the remote branch without reset.
CI run 37490633618 completed SUCCESS in all three jobs: calculation foundation,
API30 and API35 storage/installed-upgrade/regression/UI tests. Thus the fee UI
increment is now verified, including light/dark save/reopen/revise behavior.
This remains TEST signing, not a production release.

Added manual annual-rate/monthly-index entry and full reference-history screens.
Unconfirmed input stays NEEDS_REVIEW and cannot be selected for calculation.
Confirmation creates a new USER_ENTERED version, never OFFICIAL_VERIFIED.
Source details, explicit series, period, unit and change reason are retained;
stale historical edits are rejected. All old versions and calculation snapshots
remain intact. New UI checks cover Persian numeric input, draft rejection,
confirmation lineage, wrong-year rejection, stale editing and invalid months.
No schema/version/signing change. New candidate Android CI is pending; the prior
UI run is not evidence for this changed candidate.

Primary-source retrieval remains blocked: Parliament Penal Code URL timed out;
Qavanin record 198907 and Dotic 20814 were inaccessible on 2026-10-07.
No unverified annual rate or legal rule has been seeded. Next: verify this
candidate's CI; complete the remaining official-source/legal-domain gates.

### Verified reference UI and remaining release gate

Code checkpoint ace7cd0ea7bd7d15a715b09abdcb53289b121aa9 matches preserved local
f17cd6749af9b24be48f5d58a9594ef3102d1a5f (tree
8fd92931d33ecdfa6f3178adec82e0c98bdc7976). CI 37582756000 completed SUCCESS:
foundation 112665951073, API30 112665951230, API35 112665951243.
API35 logs explicitly include CalculationUiTest OK (2 tests), including manual
reference draft/confirmation/history validation. Both matrix jobs passed the
installed upgrade, storage, backup and scoped regression suites. No production
signing/version bump/release is claimed. Final changes here are documentation
only; these passing application sources do not need an identical rebuild.

Recovered historical Majlis Penal Code print text through ILO NATLEX 103202;
reviewed arsh, valuation, combination and enhancement provisions. Evidence and
limits are recorded in V10_3_LEGAL_SOURCE_REVIEW.md. The original image attachment
now exists locally; its earlier missing-path error no longer blocks this work.

Remaining external legal-evidence gate: current consolidated/amendment review,
official annual diyah circular and annual-index monthly table, article 522 and
cheque exception review (including 877). WIPO IR024 PDF retrieval returned 403;
Majlis print and Qavanin article-522 sources returned interstitials. Search
snippets and commercial calculators are not accepted substitutes. Do not enable
unverified automatic rules to mark the version complete. The first next task is
to acquire/verify that source pack, preserving the recovered primary historical
text. Then complete delay eligibility/UI, diyah/arsh engines/UI and outstanding
special tariff pathways, source-derived goldens and final production gates.

## Owner-directed usable-calculations continuation — 2026-10-07

Verified remote/local HEAD 6c9cfe75616e4d9297fa863f8f29afaae1e06a8b; clean tree.
Owner now requests credible published sources while official originals are
unavailable, and prioritizes usable installed calculations. Central publication
is approved for later deployment, but the server is not ready. The owner-only
panel must support manual entry independently of ChatGPT/international Internet,
and official-source acquisition plus approval. Iranian-network operation needs
domestically reachable auth, assets, API and storage, not merely a domestic host.
No live server or owner-authenticated panel is claimed or fabricated.

Removed manual reference editing from public navigation; historical records are
preserved. Read-only bundled-source/status screen explicitly states there is no
live update service. Added a separate REVIEWED_PUBLICATION reference status so a
reputable republication is not mislabeled as direct official verification.

New body-compensation path converts a court-assessed percentage of ordinary full
diyah or an assessed fixed amount, preserves injury/date/expert/decision context,
source snapshots and revision reasons. It does NOT diagnose injuries, determine
arsh, automatically apply taghliz or sum overlapping injuries. Those unimplemented
rules remain expressly outside this form. Published 1405 ordinary rate is stored
as separate versioned data; no other-year fallback. Arsh warning survives reports.
19 new numeric checks pass locally. New Android UI/save/restore tests are added;
candidate CI pending. This is not yet complete 10.3 or a production APK.

## Verified upgrade and bounded delay form — 2026-10-08

Verified origin `kamranvahdati-arch/VOKANO-android`, branch codex/v10.3, HEAD
21fccf63ee82329a6451f87151b0cac06367ad4f, clean before this increment.
CI 37662577692 SUCCESS: foundation 112933564622, API30 112933564540,
API35 112933564096. Logs reviewed: baseline seed/reopen, installed upgrade,
key concurrency, calculation storage/UI, backup and scoped regressions passed.
The original failure's exact root cause is not retrospectively proved.

Added historical 1399–1401 delay pack, read-only and versioned; 36 cells
rechecked visually against the saved facsimile. New simple-debt/cheque form
requires explicit case eligibility/date review and excludes special cases.
Missing months block calculation. No 1405 current index is invented or merged
with Statistical Center data. Same-month references are deduplicated.
Reports distinguish principal, damages and adjusted sum; snapshots preserve
indices, scope attestations, date basis and revision reasons. Existing schema16
and backup format are unchanged. Local delay checks: 26 existing +57 new PASS.
New Android UI/backup/revision test is added; this changed candidate CI pending.

Next: verify changed-candidate CI, complete approved entry-screen alignment.
Current index provenance, comprehensive legal special cases and permanent
production-signing access remain open release gates. No final 10.3 claim.

### Publication gate in this continuation

Local code commit: 1610560 (historical delay form and tests). Automatic approval
review rejected git push because explicit authorization to publish source/docs
to the external GitHub repository was not established by that reviewer. Do not
retry through a connector or alternate transport. Ask owner authorization for
that specific push. Remote build/emulator tests for this increment have not run.
Local Android SDK/Gradle are unavailable; pure Java validation passed as above.
No new final APK was built. Approved entry-screen work follows the changed
calculation candidate's Android verification, preserving the owner's sequence.

Owner explicitly approved publishing these changes to the named public GitHub
repository/branch on 2026-10-08. Resume publication and candidate CI. This
evidence update intentionally enables CI for the changed application tree.

## Continuation — 2026-10-08, execution tariff and release identity

Resumed remote e5adf97a9ea570c9813ad02ed7f01bac8eee2a3a in an isolated worktree;
older local continuation/74cc2ec has identical tree and was preserved. Direct
GitHub run 37709801916 is SUCCESS for foundation and API30/API35. API30 proof
was opened: upgrade and entry tests passed; light/dark entry screenshots reviewed.
The welcome actions touched with no gap and lacked primary/secondary distinction;
this increment adds theme-aware filled/outlined actions and 12dp separation.

Added the missing article-25 civil/official-document enforcement tariff path.
The official Qavanin text was retrieved again on 2026-10-08 (same source ID as
prior review). It specifies an interval; selected fee is never automatically
set to the ceiling. Exact ceiling checks precede rounding; contradictory bounds
are rejected for separate assessment. A new v2 asset preserves the v1 pack,
records the enforcement constants, and corrects the civil-order label to بند ج.
Immutable snapshots retain award, selection reason, rule pack and revision.
Local tariff test: 81 assertions PASS (13 additional boundary/history checks).
New Android save/revise/bounds/backup test added; changed-candidate CI pending.
No schema/version/application ID change. Existing 10.2/15, schema16 retained.

Original permanent signing recovery was found in the owner's stored artifacts
and decrypted privately. Recovered certificate matches the previously recorded
26055f09370416e9cd61c08246b80c57367470cdb6873e282b5b6521cf097202.
No new key created; no secret in source/log/artifact. Permanent signing access
is no longer the blocker in this session; final candidate signing/testing remains.

Still incomplete: current/historical index coverage beyond 1399–1401; prescribed
injury and taghliz/combination paths; other special tariff cases; final signed
candidate and actual production-APK upgrade verification. Do not call the
bounded four-form development app a completed 10.3 release.

### Execution tariff verified; bounded prescribed injuries

Remote 23537a7931c1586c064c9fe358d77568dcededa5 matches local 68d5c906 tree
a8991510f1f72f8ab55188dab59838940e4de3b8. Run 37746110792: foundation,
API30 and API35 all SUCCESS; the added enforcement persistence test passed.
Continued from the remote commit on a fresh local branch without deleting history.

Read/visually inspected the Majlis print PDF pages 91–92 at ILO NATLEX 103202.
Added a separate historical article-709 rule asset for seven independent head/face
injury categories (harisa through munaqqila, each below one third). The user must
supply the medical/legal classification and affirm no overlap, residual effects,
death or special ruling. Body wounds, mamuma/damigha and automatic diagnosis are
explicitly excluded. Rate-year validation and immutable source snapshots reuse
existing infrastructure. No database migration or version bump. Existing body
19 checks +15 new source-derived and scope/year validation assertions PASS locally.
Added Android classification/revision/backup UI test; changed-candidate CI pending.
This bounded pathway does not complete all injury, taghliz or combination rules.

## Active delivery continuation — 2026-10-08

Verified remote 23537a7 and preserved local 186f11f with identical published tree
77c94a3153982ecc6dda4bf4bac0da71d64e83c2. Published as 665785ffc1a30360ef31af84d6a9bd9d37538f18;
run 37798179583 is executing Android verification. Existing local branches and
worktrees remain unchanged. Work continues in continuation/v103-delivery.

Added a separately versioned death/taghliz engine and form, explicit unknown/
yes/no findings for act and death time/place, date-order validation, positive
ordinary assessed base, one-third increment only once, source/input snapshots,
immutable recalculation and canonical report integration. No schema, version,
application ID or signing configuration change. New numeric suite: 93 PASS;
existing body 19 and prescribed 15 PASS after shared test-script change.
New Android unknown/revision/report/backup test added; candidate CI is pending.

Outstanding completion gates remain: broader prescribed-injury/combination and
special-tariff coverage; dependable current index data; complete Android tests
for this changed tree; final metadata bump and same-certificate signed upgrade.
No final 10.3 claim is made by this checkpoint. First next action: publish this
changed candidate on the authorized codex/v10.3 branch and inspect its actual CI.

### Death candidate verified; current official indexes acquired

Published death candidate 3826d66c331f8c590dfcb13ff3dab8d71dbf1d3f matches local
f5b7717 tree 2802af99c4ef0cb4ca41ec2986b13d44003e148d. CI run 37798998711
completed SUCCESS in foundation/API30/API35, including new death UI, revision,
backup and existing installed-upgrade regressions. Earlier prescribed-injury
candidate 665785f also passed all three jobs in run 37798179583.

The current-index retrieval blocker is resolved: direct official CBI PDF now
provides 66 values from 1400/01 through 1405/06 (base1400). Added an immutable
separate pack and explicit UI pack selection, preserving the old base1395 pack,
reference IDs and saved reports. All 66 values match independently extracted
PDF cells and visual review. New 81 numerical checks PASS; existing delay 26
and historical 57 PASS after shared selection integration. A new Android
future-month/revision/source-provenance/backup test is pending CI for this tree.
No database/signing/version change. Do not mistake prior-candidate success for
this new tree's test result. Remaining release gates are unsupported special
legal paths and final production metadata/signature/installed upgrade validation.

### Continuation — explicit civil dispositions, 2026-10-08

Verified remote HEAD 408403614ad68409c3d004b586f476616e8487fd matches local
candidate tree e23e232086ff093dfa66d8180901f3accceaad1e. Its CI run
37800475809 completed SUCCESS. Preserved all previous work and resumed here.
Added immutable tariff v3 pack for the four explicit article-12 timing branches:
first-stage annulment before defense / rejection after defense and appeal
extinguishment before / after defense. Exact stage amount is multiplied before
rounding. Unsupported categories and incompatible stages are rejected. Old packs
remain unchanged. Inputs and rule snapshot preserve disposition and revision.
Tariff suite: 96 checks PASS. New Android persistence/revision/backup check added;
Android CI for this changed tree remains pending. No version/schema/signing change.
Remaining scope: broader tariff exceptions and injury combinations; final release
metadata, permanently signed APK, actual production upgrade and final API30/35 gates.

### Publication gate — 2026-10-08

Implementation commit: 6579c9f9329281a8a1c62549b93bda96534cbeb5, local only.
Push to origin HEAD:codex/v10.3 was rejected by automatic approval review:
explicit authorization to export code/docs to that GitHub destination required.
No alternate publication route attempted. Await owner approval for this specific
push before publishing or triggering changed-tree CI. Last verified remote is
408403614ad68409c3d004b586f476616e8487fd. Local tree passes diff whitespace checks.
Foundation: 65 PASS via java com.sun.tools.javac.Main (javac launcher absent).
Tariff: 96 PASS. Android test added but not executed for this tree; no final APK.
Next action: authorized push, inspect changed-tree API30/35 CI, then remaining
special-case scope and final production release gates described above.

## Resume 2026-10-08: independent special tariff paths

Verified local e14bfc54 tree b911fd5a and remote 6c521fba. Previous candidate
CI 37805901178 and permanent-signature upgrade 37805901383 are SUCCESS.
Prior publication-approval pause is resolved by the owner's explicit approval.
The successful signed candidate is a bounded implementation, not scope completion.

Added immutable v4 tariff pack and independent special modes for article 23
whole-case settlement/arbitration, article 5 equal counsel shares, article 14
note 3 non-compounding extra charges against the most serious charge, and
article 22 certified specialty. Source qavanin.ir IDS16814137751518820692
read again today. Each mode requires documented eligibility; combinations with
other special modes or disposition are not inferred. Exact fractions are kept
through final rounding, and reports label per-counsel amounts. New inputs and
rule asset snapshot survive revision/backup. Existing v1/v2/v3 packs unchanged.
Tariff numerical suite: 132 PASS. Added Android save/revise/restore test;
changed-tree Android build and tests pending. No schema/identity changes.
Remaining: final-by-value tariff and other special intersections, partial-payment/
instalment and insolvency delay paths, wider injuries and overlap, and final
same-key signing plus upgrade validation of the eventual completed tree.

### Partial payments, numerical and Android integration

Published special tariff tree b7c4f32b as remote68fbe508; CI37845322565
is executing (foundation already SUCCESS). Added partial-payment path with
82 new checks PASS; previous delay26/historical57/current81 and tariff132
PASS. Added Android consent/overpayment/revision/backup test; pending CI.
No schema migration; new input/result keys and references use immutable snapshots.
Next: publish changed tree and verify Android, then insolvency/instalments,
remaining tariff/injury coverage and final candidate signing gates.

### Judicial insolvency and one overdue instalment

Special tariff CI37845322565 completed SUCCESS on foundation/API30/API35.
Partial-payment tree86a34444 published as d3d1e52b; CI37845882999 pending.
Added two explicit independent ruling824 pathways: clamp accrual to the
judicially established insolvency date, or compute one documented overdue
instalment from its own due date. No automatic judgment/default determination,
no automatic combination with partial payments, no claim to bankruptcy coverage.
Requested/effective dates and the scope of each result are preserved. New
numerical16 PASS; added Android cutoff/future-report-date/instalment/revision/
backup test pending. Previous numerical delay suites still pass. Next publish
this tree and verify CI; then remaining scope and final changed-candidate signing.
