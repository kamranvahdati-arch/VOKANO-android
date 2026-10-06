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
