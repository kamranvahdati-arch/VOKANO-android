# Owner follow-up and inspected design references

Continue Android 10.3, first database-upgrade reliability and calculation sources,
then visual alignment before final delivery. Do not start a web implementation.

## Entry flow accepted by owner

- A branded entry page offers initial lawyer-profile registration and app entry.
- If no completed profile exists, entry leads to registration. Preserve already
  saved profiles; never force existing users to register again or clear data.
- After initial registration, entry is direct unless the user enabled the
  optional local password/biometric lock. Preserve password fallback for biometrics.
- After platform deployment, public-release registration MUST verify the phone
  using a real server-issued SMS OTP. Profile fields alone must not become a
  verified identity. This is separate from the optional local lock.
- Server, SMS sending, OTP verification and public authentication are not deployed.

## Images actually inspected this session

All sixteen supplied image files were opened successfully after attachment
availability recovered. Earlier missing-path errors no longer describe current
access. No image regeneration or identity modification was performed.

The owner's described entry screen appears on the phone in
`B7392687-9828-499C-9028-FA4F3B8ACAC3.jpeg` (attachment 15): navy background,
white/cyan V mark, Persian VOKANO name, tagline, blue/cyan primary entry action,
outlined registration action and small footer. This is a promotional concept,
not an implemented screen or evidence of store availability.

`54F28B3A-5FED-4E73-8531-018AB3E03CD7.jpeg` (18) supplies visual palette labels
#0A2D5B, #1478C8, #00C2A8, #6B7280 and an IRANSans font suggestion. The first
three already appear in the app's theme presets. Do not assume a font licence
or copy generated lettering as UI text. Reuse existing approved logo assets.

References 6, 8, 11, 12, 13, 14, 15 and 16 show line icons, white rounded cards,
navy framing and cyan accents. References 7, 9, 10 and 17 depict the separate
brand ambassador. References 1, 4 and 5 depict the owner's portrait; these are
not interchangeable identities. No visual redesign has yet been implemented.

## Delay-source progress and remaining blocker

The facsimile of the CBI table through 1401 was downloaded and inspected:
https://www.ekhtebar.ir/wp-content/uploads/2023/04/shakhes-2.jpg
Forwarding-letter republication:
https://shenasname.ir/qaza/53343-مهریه-و-تأخیر-تأدیه-۱۴۰۲
Three historical rows (1399–1401) are saved in
`legal-evidence/delay-indices-1399-1401.properties`, not activated in the app.

Current secondary sources were compared on 2026-10-07:
https://edalatsara.com/tools/محاسبه-آنلاین-خسارت-تاخیر-تادیه
https://vakilsoal.com/شاخص-محاسبه-تأخیر-تأدیه-بر-اساس-داده/
They disagree for Khordad 1405: 3092.3 versus 3093.5. Vakilsoal additionally
contains the previously documented inconsistent annual average for 1399.
Edalatsara explicitly describes temporary substitution of Statistical Center
indices pending CBI release, while saying its current data is now CBI through
Shahrivar 1405. That statement alone does not resolve the conflict or prove
every cell's provenance. Do not silently blend series or choose a convenient value.

The CBI 4930 page retrieved by curl contained an English navigation shell, not
the expected numerical table; search-service attempts timed out. No current
original table has yet been retrieved. Further verification is required before
claiming a current-year calculation is authoritative.

The operative holdings of rulings 812 and 850 were read in Edalatsara's full-text
republications: 812 distinguishes cheque-date accrual from ordinary demand rules;
850 specifies the index ratio and excludes compound interest. Keep holdings
separate from case reports and the prosecutor's submissions. Eligibility,
partial payments, insolvency/bankruptcy and other special cases remain to be
implemented/reviewed; the current UI continues to block final delay calculation.
