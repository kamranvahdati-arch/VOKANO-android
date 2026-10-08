# Legal source review — VOKANO 10.3

Review date: 2026-10-06 UTC. Documentation checkpoint, NOT a release rule pack.
No rate, formula, schema, application behavior or saved calculation changed.

## Evidence register

### TARIFF-1398-12-28

- Instrument: آیین‌نامه تعرفه حق‌الوکاله، حق‌المشاوره و هزینه سفر وکلای دادگستری.
- Issuer/date: رئیس قوه قضاییه, 1398/12/28; 34 articles and 10 notes.
- Primary official consolidated publication, all 34 articles read:
  https://qavanin.ir/Law/TreeText/?IDS=16814137751518820692
- Official consolidation relationships inspected:
  https://qavanin.ir/Law/StatusIndex/?IDS=16814137751518820692
  The retrieved affecting-instruments table contained no entries; the affected
  instruments listed the 1378/04/28 and 1385/04/27 tariffs. This is an observation
  of the retrieved page, NOT proof that no subsequent instrument exists.
- Independent institutional cross-check, all 34 articles read:
  https://icbar.ir/DYN/21/آیین_نامه_تعرفه_حق_الوكاله_حق_المشاوره_و_هزینه_سفر
  Publisher: Central Bar Association. Its text contains an Ekhtebar compilation
  credit and typographical defects. Use the official consolidated text above
  as the authoritative transcription, not this republication's typos.
- Qavanin basic-attributes link returned an interstitial rather than metadata.
  Gazette publication number/date and original signature facsimile were NOT
  retrieved. Do not fabricate them or equate adoption date with publication date.
- Status: official text reviewed; a dated 1398 rule-version specification can
  be extracted. Final current-applicability/release review remains open,
  particularly cross-statute changes to jurisdictions and appealability.
- Candidate legal basis version: `IR-TARIFF-1398-12-28.review-2026-10-06`.
  This ID is documentation only; no runtime reference has been seeded.

## Article coverage and implementation constraints

All monetary amounts below are **rials**. Numeric limits, percentages and stage
weights belong in versioned reference data, not scattered engine constants.
Ranges are not a mandate to choose the maximum, minimum or midpoint. Where the
law leaves discretion, report the interval and require a supported user-selected
amount; preserve that choice and its explanation in the calculation snapshot.

| Article | Extracted computational relevance | Required handling |
| --- | --- | --- |
| 1 | Definitions of Bar, Center, Fund and tax law | Metadata; do not conflate institutions. |
| 2 | Agreement governs lawyer/client; absent agreement tariff applies; lower agreement matters for opposing party | Separate contractual amount, tariff amount and recoverable amount. |
| 3 | Record fee and rial equivalent of noncash fee; opponent's liability limited by tariff | Never label full agreed excess as automatically recoverable. Tax advance is distinct. |
| 4 | Stamp deficiency procedure | No fee formula; no new tax workflow. |
| 5 | Equal division among multiple counsel absent other agreement | Explicit share inputs; no modification of existing collaboration/accounting. |
| 6 | New counsel versus delegated continuation and stamp obligations | Context warning only; no tax engine. |
| 7 | Gratuitous representation of specified relatives and exemptions | Context only; do not turn exemption into a general zero-fee rule. |
| 8 | Appointed/legal-aid fee is twice the applicable minimum | Require category with an identified minimum; do not invent a minimum for a maximum-only category. |
| 9 | Pecuniary claims; special final-by-value branch and progressive brackets | See bracket specification below; explicit finality selection, no guessed appeal threshold. |
| 10 | Third-party/interpleader/counterclaim work corresponds to the relevant stage | Explicit stage and separate matter identity; prevent duplicate summation. |
| 11 | Default judgment and objection; existing counsel for default winner has no extra defense fee for objection | Require role and prior representation facts. |
| 12(a,b) | Pre-defense petition annulment: quarter of first stage; post-defense petition rejection: half | Do not apply fractions to the entire case instead of stage amount. |
| 12(c) | Specified nonhearing/dismissal/retrial refusal orders: fee provided for judgment | Preserve exact order category; not all dismissals are equivalent. |
| 12(d,e) | Appeal extinguishment before/after defense: quarter/half of appeal stage | Defense timing required. |
| 12(f), note | Civil-order objection: 1,000,000–100,000,000; remand to merits earns stage fee | Keep objection and resumed merits services separate. |
| 13(a) | Family, financial claims arising from marriage, noncontentious matters: 5,000,000–200,000,000 | Do not send marital financial claims blindly through general financial brackets. |
| 13(b) | Other nonpecuniary/no statutory valuation required: 4,000,000–300,000,000 | Range, not one universal percentage. |
| 14(a1) | Criminal-one/military-one/revolution: death, amputation, life or grade-one imprisonment: 50,000,000–2,000,000,000 | Court and punishment category required. |
| 14(a2,a3) | Same courts, grades 2/3: 30,000,000–1,000,000,000; others: 10,000,000–500,000,000 | Versioned category rows. |
| 14(b1) | Criminal-two/military-two/juvenile: hudud, diyah, grades 4/5: 10,000,000–500,000,000 | Do not infer court solely from a number entered by the user. |
| 14(b2,b3) | Same courts, grade 6: 5,000,000–300,000,000; others: 2,000,000–200,000,000 | Versioned category rows. |
| 14(c) | Prosecutor-order objection: 1,000,000–150,000,000 | Separate service. |
| 14(d) | Criminal cassation, retrial request, article-477 request: 2,000,000–200,000,000 | Distinguish service from article 17 appellate-review pathway; ambiguous overlap needs review. |
| 14(e) | Restricted article-48 representation: 50,000,000–500,000,000 | Applicability must be supplied, not inferred. |
| 14 note 1 | Prosecutor 50%, first instance 30%, appeal 20%; absent prosecutor its share goes to trial; final trial receives appeal share | Separate stage availability/finality inputs; explain reassignment. |
| 14 note 2 | Accepted cassation/retrial with reversal and resumed representation adds new stage fee | New service/stage, not silent revision of prior fee. |
| 14 note 3 | Most serious charge tariff plus 20% of that fee per additional offense | Require identified most serious charge; no compounding. |
| 15 | Dispute council and review correspond to first/appeal stage | Do not silently extend old court labels to new peace-court jurisdiction; verify applicable procedural statute. |
| 16 | Supreme Court civil pecuniary/nonpecuniary work uses appeal tariff | Distinct civil cassation stage. |
| 17 | Criminal judgment review at Supreme Court uses appeal tariff | Resolve relationship to article 14(d) before automation. |
| 18 | Counsel taking over after reversal: half pre-reversal fee | Need prior fee and scope; do not stack blindly with article 14 note 2. |
| 19 | Listed disciplinary prosecutors/courts: maximum 20,000,000 | No minimum invented. |
| 20 | Administrative Justice Court / governmental sanctions: 4,000,000–500,000,000; other nonjudicial forums e.g. labor: 4,000,000–200,000,000 | Forum-specific reference rows. |
| 21 | Articles 9/13/20: first stage 60%, appeal 40% | Apply only to stated categories; special final-by-value branch needs explicit reconciliation. |
| 22, note | Stage payment at start unless agreement; certified specialization within its scope adds 10% | Certificate/scope evidence required; installment scheduling is separate. |
| 23 | Specified arbitration/out-of-court resolution and settlement: whole-case fee | Require qualifying disposition; avoid extra whole-case amount on top of the same fee. |
| 24 | Dismissal/death/incapacity/resignation/end of mandate: full stage if ready for judgment; otherwise proportional amount determined by Bar/Center | No invented automatic work-completion percentage. |
| 25 | Civil enforcement/official instruments: minimum 4,000,000, maximum 2% of award; other enforcement: 4,000,000–150,000,000 | If 2% is below minimum, flag incompatible bounds; do not silently clamp. |
| 26(a) | Drafting petition/complaint/defense/notice: maximum 50,000,000 | Applies absent power of attorney and agreement; no invented minimum. |
| 26(b,c) | Hourly advice: 500,000–5,000,000; file study: 2,000,000–50,000,000 | Exact decimal duration for hourly work; unit explicit. |
| 26 note | Reporting prior-year service income and received installments | Scope note only; no new accounting/tax feature. |
| 27 | Special clergy courts follow corresponding categories | Require identified corresponding category. |
| 28 | Other unprovided cases: 10,000,000–250,000,000 | Not an escape hatch for an unknown or unsupported legal rule. |
| 29 | Institutional contribution obligations | Recorded for coverage only; outside requested four calculation domains. |
| 30, note | Travel absent agreement: actual expenses plus daily allowance, subject to conditions | Same province outside licensed district: 1,500,000/day; other province: 3,000,000/day; foreign travel references director-general allowance, not an invented number. Ineligible concentration-of-practice cases excluded. |
| 31 | Excess stamp certificate | No fee arithmetic; existing workflows untouched. |
| 32 | Continuation for heirs/guardian/successor without repeat stamp | No fee arithmetic; existing workflows untouched. |
| 33 | Governmental client stamp withholding obligations | No tax engine added. |
| 34 | Tax assessment provision | Recorded for completeness, not automated in this module. Later tax-law interaction not reviewed. |

### Article 9 progressive reference specification

Only the non-final-by-value branch uses these marginal tiers:

| Slice of claim value (IRR) | Percentage |
| --- | --- |
| 0–500,000,000 | 8 |
| Above 500,000,000 through 2,000,000,000 | 7 |
| Above 2,000,000,000 through 10,000,000,000 | 5 |
| Above 10,000,000,000 through 30,000,000,000 | 4 |
| Above 30,000,000,000 | 3 |

The separate judgment-final-by-value wording states 10% of claim value.
Do not decide finality from the tariff alone or silently multiply that branch
by article 21's 60%: the interaction must be resolved/documented before enabling
it. Unknown valuation is not zero; article 9's note redirects a case rejected
before final valuation to article 13(b).

### Manually derived fixture candidates — NOT executed tests

Assumptions for these candidates: article 9 progressive branch, no special
family category, no settlement, specialist uplift, prior counsel, special order,
multiple lawyer adjustment or manual override. Stage amounts use article 21.

| Claim IRR | Whole tariff IRR | First 60% | Appeal 40% | Independent derivation |
| --- | --- | --- | --- | --- |
| 500,000,000 | 40,000,000 | 24,000,000 | 16,000,000 | 500,000,000 × 8/100 |
| 2,000,000,000 | 145,000,000 | 87,000,000 | 58,000,000 | 40,000,000 + 1,500,000,000 × 7/100 |
| 10,000,000,000 | 545,000,000 | 327,000,000 | 218,000,000 | 145,000,000 + 8,000,000,000 × 5/100 |
| 30,000,000,000 | 1,345,000,000 | 807,000,000 | 538,000,000 | 545,000,000 + 20,000,000,000 × 4/100 |
| 31,000,000,000 | 1,375,000,000 | 825,000,000 | 550,000,000 | 1,345,000,000 + 1,000,000,000 × 3/100 |

These expected values were derived from the official regulation, not an online
calculator. They are draft golden inputs for future engine comparisons; they
do NOT establish a passing legal golden suite. Boundary ±1 rial, exact fractional
rounding, invalid/zero/overflow, unavailable references and historic versions
remain to be tested with an explicit rounding policy (not a claimed legal rule).

## Other legal sources: unfinished, not approved runtime rules

| Domain | Remaining verification |
| --- | --- |
| Diyah / arsh | Official current Penal Code book-four text, article 449, effective valuation date, injury ratios, combination/non-overlap rules, articles 555–557 and related exceptions; yearly circular separately. Do not confuse book-five article 555 with book-four article 555. |
| Yearly diyah | User's 1405 example 21,000,000,000 IRR is NOT independently verified official reference data. No annual rate seeded. |
| Ordinary delay | Official article 522 and ruling 850 of 1403/05/16, annual-index series published by month, eligibility and effective start-date qualifications. Unofficial search matches are leads only. |
| Cheque delay | Official ruling 812 of 1400/04/01 and incorporated cheque statutes, scope/exceptions. Separate mode; no unqualified application to every document labeled cheque. |
| Current developments | Search surfaced Dotic news 20814 concerning ruling 877 and post-cessation bankruptcy delay. Full page retrieval failed. Number/title alone are NOT evidence of its operative holding. Check before making any bankruptcy eligibility assertion; not a new bankruptcy feature. |

Useful unfinished retrieval leads (NOT verified holdings):

- Penal Code parliament source: https://rc.majlis.ir/fa/law/show/845048
  Previously returned 502; no official full-code review completed.
- Dotic lead: https://dotic.ir/news/20814/رأی-وحدت-رویه-شماره-877-هیات-عمومی-دیوان-عالی-كشور-با-موضوع-تعلق-خسارت-تاخیر-تادیه-به-مطالبات-بستانکاران-برای-ایام-بعد-از-تاریخ-توقف
  Retrieval returned Internal Error on 2026-10-06.
- Institutional cheque-ruling lead:
  https://guilanbar.ir/رای-وحدت-رویه-شماره-813/
  Search title says 812 while URL says 813; inspect the actual signed text before
  assigning a ruling identity. Not yet opened or accepted.

## Exact continuation

1. Preserve this tariff article extraction; do not repeat a general project audit.
2. Retrieve official remaining domain texts and check applicable changes; resolve
   tariff overlaps above before automating those branches. An unverified branch
   must remain unavailable/explicitly manual, not a guessed legal result.
3. Then implement versioned reference packs and domain engines with independent
   goldens; persistence/UI/migration/export/upgrade work remains in the main
   checkpoint. No application source changed in this evidence-only increment.

## Subsequent evidence and numerical implementation — 2026-10-06

Official ruling 850, dated 1403/05/16, was retrieved in full from the
Presidency legal publication service: https://dotic.ir/news/17037/
The operative section (not merely the prosecutor's opinion) was reviewed.
It specifies annual indexes published in monthly tables: principal multiplied
by payment index divided by starting index yields debt including damages;
compound interest is excluded. The retrieved text requires statutory eligibility.
This verifies that historical holding, not an exhaustive current-law review.

`CalculationDelayMath` now implements that numerical ratio, keeps principal,
adjusted debt and damages separate, preserves exact rational output and applies
explicit final rounding. It retains distinct ORDINARY_DEBT/CHEQUE modes but
DOES NOT determine entitlement, select a legal starting date or certify a cheque.
Both require an explicit reviewed start date and recorded basis from the future
legal assessment layer. No official index data or release rule pack is seeded.
Reference year/month, series, validity, confirmation, units and overflow are
checked. Declining indexes are rejected pending a supported policy; the code
does not invent negative damages or silently clamp them to zero.

Remaining blockers: direct official article 522; ruling 812 and cheque scope;
Penal Code/diyah/arsh; annual rates/indexes; later-change review including 877.
The parliament URL returned an interstitial, not statute text. Dotic print/20814
failed retrieval. Search snippets and unrelated book-five provisions were not
accepted as substitutes. The prior tariff extraction remains preserved.

Independent synthetic numerical checks: 1,000,000 x 150/100 = 1,500,000
(total), 500,000 (damages); 2 x 4/3 = 8/3 exact, with explicit rounding.
These exercise the reviewed arithmetic, NOT legal eligibility or real index data.

## Continued evidence — 2026-10-06, tariff/storage increment

The Gilan Bar page titled ruling 812 (despite its slug ending in 813) links an
image of the signed Supreme Court ruling, number 812, dated 1400/04/01:
https://guilanbar.ir/wp-content/uploads/2021/07/images_1398_09_812.jpeg.webp
The actual image was opened and read. Its operative paragraph selects the
date on the cheque and distinguishes its statutory regime from article 522.
This is primary-document evidence hosted by the Bar, not merely a snippet or
the page title. It does not certify all later exceptions or applicability to
every document described as a cheque. Current-law/exception review remains open.

Direct article-522 Qavanin record 502404499976366661 timed out (TreeText and
PrintText). Parliament penal-code record 845048 returned an interstitial rather
than statute text. Qavanin legacy record 198907 and Tehran University legal
office page 2984 were also unavailable. These failures are not legal findings.

The tariff arithmetic now loads a separate UTF-8 properties rule pack. It
implements only the reviewed article-9 progressive branch, ordinary article-21
stage shares, article-14 basic criminal stage redistribution, and the expressly
listed ranges/services. Manual selections must remain within their interval;
maximum-only provisions retain an unknown minimum. The pack labels its review
as historical-text-reviewed; it does not infer commencement from adoption or
claim current applicability has been exhaustively established.

68 source-derived/boundary/validation assertions passed locally. The independent
financial golden candidates above now execute against the engine. This is a
limited tariff golden suite, not completion of all legal golden requirements.
Special dispositions, final-by-value interaction, multiple counsel/charges,
specialization, travel and the 14(d)/17 ambiguity are not implemented. No UI may
silently route those special circumstances through the ordinary path.

## Recovered primary Penal Code text — 2026-10-07

ILO NATLEX record 103202 identifies the Majlis as source. Its 94-page Persian
PDF preserves the Majlis print-version URL 845048 and print date 2016-12-18:
https://natlex.ilo.org/dyn/natlex2/natlex2/files/download/103202/penal%20code.pdf
This recovers primary historical text, NOT current consolidated status.

Reviewed printed pages 63, 67–68, 73–76 (PDF indices 62, 66–67, 72–75):
- 449: judicial assessment of arsh with expert input; no software assessment.
- 490: payment-time valuation, except an agreed fixed amount.
- 538–548: multiple-injury combination depends on causation, injury identity,
  location and progression; no unconditional sum. Article 548 caps one injury's
  arsh by its prescribed counterpart.
- 549: annual announcement is separate reference data.
- 555–557: one-third death enhancement has act/death temporal or sanctuary
  conditions; sacred-month boundaries use legal sunset. Injury/benefit losses
  do not receive this enhancement; article 556 contains fetal-life exceptions.
- 550–552, 560–562: sex-related rules and compensation-fund distinctions need
  separate treatment; do not silently halve every output or conflate fund
  compensation with offender liability.

No new automatic rule is enabled by this review. Remaining: amendments/current
status, annual official circular, injury-specific schedule, exception rulings.
WIPO IR024 identifies a Persian civil-procedure PDF, but its download returned
403; article 522 has still not been directly reviewed from that document.

## Credible-publication path requested by owner — 2026-10-07

Full Tasnim report 3620901 (1405/03/29) and Nournews report 303204
(1404/12/27), both reporting the judiciary announcement, agree on ordinary full
diyah of 21,000,000,000 IRR from 1405/01/01:
https://tasnimnews.ir/fa/news/1405/03/29/3620901/
https://nournews.ir/fa/news/303204/
The separate 1405 data pack records REVIEWED_PUBLICATION, not OFFICIAL_VERIFIED;
publication date is not fabricated as circular date, and no circular number is
invented. Assessed-percentage conversions use this explicitly labeled source.

Article 522 read in full at https://nezamat.ir/post-31264/ (republication, not
direct official access): ordinary current-money debt requires demand, ability,
refusal, substantial index change, and consideration of alternative settlement.
This evidence advances the eligibility review, not a blanket current-law opinion.

CBI page 4930 was identified but table retrieval timed out. An independent legal
site table/PDF was inspected at vakilsoal.com; the annual-average entry for 1399
exceeds every listed monthly value. That inconsistency and missing independent
confirmation prevent accepting this table as a verified data pack. No guessed
indices or copied calculator outputs are seeded. Delay UI remains gated pending
a dependable index source and reviewed eligibility/exception handling.

## Historical delay activation review — 2026-10-08

Read the stored CBI facsimile again and checked the 36 cells for 1399–1401.
The application pack preserves those values and excludes annual averages.
Article 522 republication at nezamat.ir/post-31264 was re-read, operative
paragraph: demand, financial ability, refusal, substantial index change and
contrary settlement matter. The UI asks the lawyer to review those case facts
and record the start-date basis; it does not algorithmically decide them.
The cheque mode requires explicit review of the previously inspected ruling
812 and equal cheque/start dates. Exceptions are excluded, not deemed absent.
Ruling 850 historical holding and exact-ratio implementation are unchanged.

Ruling 877 republication at daftarvakil.ir/قوانین-و-مصوبات/195484/ was read.
It concerns remaining bankruptcy assets and creditor loss, not a generic
cheque rule. Bankruptcy is explicitly outside this form; no exception engine
is claimed. Direct Dotic re-fetch failed. Current indices remain unverified.

## Article 25 continuation — 2026-10-08

Re-read official Qavanin record 16814137751518820692, article 25. The independent
civil enforcement branch is now represented in the v2 data asset and engine:
4,000,000 IRR minimum and exact 2% award ceiling. Choice within the interval is
explicit; no automatic maximum. Where the ceiling is below the minimum the
engine rejects instead of inventing precedence. Service uses WHOLE, with no
60/40 stage allocation. Source-derived examples: award 200,000,000 => a single
4,000,000 bound; award 1,000,000,000 => 4,000,000–20,000,000. At 200,000,049,
4,000,001 exceeds the exact 4,000,000.98 ceiling and is rejected before rounding.
This does not resolve remaining special tariff interactions or current indexes.

### Article 709 bounded schedule — 2026-10-08

Downloaded original Majlis print PDF from NATLEX 103202 and visually inspected
pages 91–92. Implemented only the seven head/face categories through munaqqila;
exact fractions and a historical-review version live in their own properties
asset. No body-member fraction substitution, residual-effect addition, automatic
injury classification or >=one-third rule is inferred. Independently derived
1405 fixtures for the 1,2,3,4,5,10,15 percent rows are respectively 210m, 420m,
630m, 840m, 1,050m, 2,100m, 3,150m IRR. Other rates cannot silently fall back.
Current consolidated-status verification is not implied by historical text.
