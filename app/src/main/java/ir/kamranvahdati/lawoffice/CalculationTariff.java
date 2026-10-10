package ir.kamranvahdati.lawoffice;

import java.io.IOException;
import java.io.Reader;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** Exact tariff arithmetic over an explicitly selected, immutable rule pack.
 * Does not decide jurisdiction, appealability, special-case eligibility or recoverability.
 * Adoption date is metadata, NOT an inferred effective date.
 */
final class CalculationTariff {
    static final String ENGINE_VERSION = "tariff-math/6";
    enum Stage { WHOLE, FIRST, APPEAL, CIVIL_CASSATION, PROSECUTOR }
    enum Disposition { ORDINARY, ANNUL_BEFORE_DEFENSE, REJECT_AFTER_DEFENSE, APPEAL_DROP_BEFORE_DEFENSE, APPEAL_DROP_AFTER_DEFENSE, NONHEARING_OR_RETRIAL_REFUSAL }
    enum Special { NONE, SETTLEMENT, EQUAL_COUNSEL, MULTIPLE_CHARGES, CERTIFIED_SPECIALTY, APPOINTED_AID, CONTINUED_AFTER_REVERSAL }
    enum Family { CIVIL, CRIMINAL, SERVICE }

    static final class Band {
        final long upper;
        final CalculationArithmetic.Fraction rate;
        Band(long upper, String percent) {
            if (upper <= 0) throw invalid("حد پله تعرفه باید مثبت باشد");
            this.upper = upper; rate = CalculationArithmetic.percent(percent);
        }
    }

    static final class Range {
        final Long minimum;
        final long maximum;
        final String article, label;
        final Family family;
        Range(Long minimum, long maximum, String article, String label, Family family) {
            if (maximum <= 0 || minimum != null && (minimum < 0 || minimum > maximum))
                throw invalid("حدود تعرفه ناسازگار است");
            if (family == null) throw invalid("گروه تعرفه الزامی است");
            this.minimum = minimum; this.maximum = maximum;
            this.article = CalculationReference.required(article, "ماده تعرفه");
            this.label = CalculationReference.required(label, "عنوان تعرفه");
            this.family = family;
        }
    }

    static final class Rules {
        final String version, sourceUrl, sourceTitle, adoptionDate, reviewDate, serializedRules;
        final Map<Disposition, CalculationArithmetic.Fraction> dispositions;
        final Map<Special, CalculationArithmetic.Fraction> specialFactors;
        final List<Band> bands;
        final Map<String, Range> ranges;
        final CalculationArithmetic.Fraction civilFirst, civilAppeal, financialFinal;
        final String finalOpinionUrl, finalOpinionNumber;
        final CalculationArithmetic.Fraction replacementRate;
        final Long enforcementMinimum;
        final CalculationArithmetic.Fraction enforcementMaximumRate;
        final CalculationArithmetic.Fraction criminalProsecutor, criminalFirst, criminalAppeal;

        Rules(Reader reader) throws IOException {
            if (reader == null) throw invalid("بسته قواعد موجود نیست");
            StringBuilder raw=new StringBuilder();char[] buffer=new char[4096];int count;
            while((count=reader.read(buffer))!=-1){raw.append(buffer,0,count);if(raw.length()>100000)throw invalid("بسته تعرفه بیش از حد بزرگ است");}
            serializedRules=raw.toString();
            Properties p = new Properties(); p.load(new java.io.StringReader(serializedRules));
            version = required(p, "version"); sourceUrl = required(p, "source.url");
            sourceTitle = required(p, "source.title");
            adoptionDate = CalculationReference.date(required(p, "adoption.date"));
            reviewDate = required(p, "review.date.utc");
            if (!"historical-text-reviewed".equals(required(p, "review.status")))
                throw invalid("وضعیت بررسی متن بسته قواعد شناخته‌شده نیست");
            ArrayList<Band> values = new ArrayList<>(); long previous = 0;
            for (String row : required(p, "financial.bands").split(";")) {
                String[] pair = row.split(":", -1);
                if (pair.length != 2) throw invalid("پله تعرفه نامعتبر است");
                Band b = new Band(Long.parseLong(pair[0]), pair[1]);
                if (b.upper <= previous) throw invalid("پله‌های تعرفه مرتب نیستند");
                previous = b.upper; values.add(b);
            }
            if (previous != Long.MAX_VALUE) throw invalid("پوشش پله آخر ناقص است");
            bands = Collections.unmodifiableList(values);
            Map<Disposition, CalculationArithmetic.Fraction> ds = new LinkedHashMap<>();
            for(Disposition d:Disposition.values()) {
                if(d==Disposition.ORDINARY)continue;
                String key="disposition."+d.name();
                if(p.containsKey(key)) {
                    CalculationArithmetic.Fraction f=percent(p,key);
                    if(f.numerator.signum()<=0||f.numerator.compareTo(f.denominator)>0)
                        throw invalid("ضریب قرار باید مثبت و حداکثر یک باشد");
                    ds.put(d,f);
                }
            }
            dispositions=Collections.unmodifiableMap(ds);
            Map<Special, CalculationArithmetic.Fraction> sf=new LinkedHashMap<>();
            for(Special special:Special.values())if(special!=Special.NONE&&p.containsKey("special."+special.name())) {
                CalculationArithmetic.Fraction factor=special==Special.APPOINTED_AID?
                    CalculationArithmetic.Fraction.decimal(CalculationArithmetic.decimal(required(p,"special."+special.name()))).multiply(CalculationArithmetic.Fraction.of(1,100)):
                    percent(p,"special."+special.name());
                if(factor.numerator.signum()<=0)throw invalid("ضریب ویژه باید مثبت باشد");
                sf.put(special,factor);
            }
            specialFactors=Collections.unmodifiableMap(sf);
            replacementRate=p.containsKey("replacement.percent")?percent(p,"replacement.percent"):null;
            if(replacementRate!=null&&replacementRate.numerator.signum()<=0)throw invalid("ضریب وکیل پس از نقض باید مثبت باشد");
            financialFinal=p.containsKey("financial.final.percent")?percent(p,"financial.final.percent"):null;
            finalOpinionUrl=financialFinal==null?"":required(p,"financial.final.opinion.url");
            finalOpinionNumber=financialFinal==null?"":required(p,"financial.final.opinion.number");
            if(financialFinal!=null&&(financialFinal.numerator.signum()<=0||financialFinal.numerator.compareTo(financialFinal.denominator)>0))
                throw invalid("نرخ حکم قطعی باید مثبت و حداکثر صد درصد باشد");
            civilFirst = percent(p, "civil.first"); civilAppeal = percent(p, "civil.appeal");
            criminalProsecutor = percent(p, "criminal.prosecutor");
            criminalFirst = percent(p, "criminal.first"); criminalAppeal = percent(p, "criminal.appeal");
            requireOne(civilFirst.add(civilAppeal));
            requireOne(criminalProsecutor.add(criminalFirst).add(criminalAppeal));
            Map<String, Range> rs = new LinkedHashMap<>();
            for (String id : required(p, "range.ids").split(",")) {
                id = id.trim(); String key = "range." + id + ".";
                String min = required(p, key + "min");
                Range range = new Range("unknown".equals(min) ? null : Long.valueOf(min),
                        Long.parseLong(required(p, key + "max")), required(p, key + "article"),
                        required(p, key + "label"), Family.valueOf(required(p, key + "family")));
                if (rs.put(id, range) != null) throw invalid("شناسه تعرفه تکراری است");
            }
            ranges = Collections.unmodifiableMap(rs);
            // Old historical snapshots remain readable; absence never guesses a new rule.
            boolean hasMin=p.containsKey("enforcement.minimum"), hasRate=p.containsKey("enforcement.maximum.percent");
            if(hasMin!=hasRate)throw invalid("بسته اجرای احکام ناقص است");
            enforcementMinimum=hasMin?Long.valueOf(required(p,"enforcement.minimum")):null;
            enforcementMaximumRate=hasRate?percent(p,"enforcement.maximum.percent"):null;
            if(hasMin&&(enforcementMinimum<=0||enforcementMaximumRate.numerator.signum()==0))
                throw invalid("حدود اجرای احکام باید مثبت باشد");
        }

        private static String required(Properties p, String key) {
            return CalculationReference.required(p.getProperty(key), key);
        }
        private static CalculationArithmetic.Fraction percent(Properties p, String key) {
            return CalculationArithmetic.percent(required(p, key));
        }
        private static void requireOne(CalculationArithmetic.Fraction f) {
            if (!f.numerator.equals(f.denominator)) throw invalid("جمع سهم مراحل باید صد درصد باشد");
        }
    }

    static final class Result {
        final String ruleVersion, sourceUrl, sourceTitle, article, exactRials, selectedBasis;
        final CalculationArithmetic.Fraction exactAmount;
        final Stage stage;
        final long rials;
        final RoundingMode rounding;
        final List<String> steps;
        Result(Rules rules, String article, Stage stage, CalculationArithmetic.Fraction exact,
                String selectedBasis, List<String> steps, RoundingMode rounding) {
            ruleVersion = rules.version; sourceUrl = rules.sourceUrl; sourceTitle = rules.sourceTitle;
            this.exactAmount = exact;
            this.article = article; this.stage = stage; this.exactRials = exact.toString();
            this.selectedBasis = selectedBasis; this.rounding = rounding;
            rials = exact.roundedRials(rounding);
            this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
        }
    }

    /** Article 9 progressive and explicit final-by-value branches. Family, dispositions and
     * specialist/multiple-counsel adjustments require their separate reviewed pathways.
     * The caller must record why this rule version and category apply to this matter.
     */
    static Result financial(Rules rules, long claimRials, Stage stage, String applicabilityBasis,
            boolean finalByValue, RoundingMode rounding) {
        require(rules, stage, rounding);
        String basis = CalculationReference.required(applicabilityBasis, "مبنای انطباق تعرفه با پرونده");
        if(finalByValue) {
            if(rules.financialFinal==null)throw invalid("بسته تاریخی فاقد شاخه حکم قطعی از حیث بها است");
            if(stage!=Stage.WHOLE&&stage!=Stage.FIRST)throw invalid("حکم قطعی از حیث بها در این مسیر مرحله تجدیدنظر یا فرجام ندارد");
            if(claimRials<=0)throw invalid("بهای خواسته باید مشخص و مثبت باشد");
            CalculationArithmetic.Fraction exact=CalculationArithmetic.Fraction.of(claimRials,1).multiply(rules.financialFinal);
            List<String> steps=new ArrayList<>();
            steps.add("صدر ماده ۹؛ قطعیت از حیث بها به تصریح کاربر: "+claimRials+" × "+rules.financialFinal+" = "+exact+" ریال");
            steps.add("کل حق‌الوکاله این مسیر؛ سهم ۶۰٪ دوباره اعمال نمی‌شود. نظریه مشورتی بازنشرشده "+rules.finalOpinionNumber+"؛ "+rules.finalOpinionUrl);
            steps.add("نصاب صلاحیت یا قطعیت از روی مبلغ حدس زده نشده است؛ این فرض با صرف قطعیت ناشی از عدم اعتراض متفاوت است.");
            return new Result(rules,"9-final",stage,exact,basis,steps,rounding);
        }
        if (claimRials <= 0) throw invalid("بهای خواسته باید مشخص و مثبت باشد");
        CalculationArithmetic.Fraction amount = CalculationArithmetic.Fraction.of(0, 1);
        List<String> steps = new ArrayList<>(); long previous = 0;
        for (Band band : rules.bands) {
            if (claimRials <= previous) break;
            long slice = Math.min(claimRials, band.upper) - previous;
            CalculationArithmetic.Fraction part = CalculationArithmetic.Fraction.of(slice, 1).multiply(band.rate);
            amount = amount.add(part);
            steps.add("ماده ۹: " + slice + " × " + band.rate + " = " + part + " ریال");
            previous = band.upper;
        }
        CalculationArithmetic.Fraction share = share(rules, Family.CIVIL, stage, true, false);
        steps.add("مواد ۱۶ و ۲۱ حسب مرحله: " + amount + " × " + share);
        return new Result(rules, "9/16/21", stage, amount.multiply(share), basis, steps, rounding);
    }

    /** Returns a supported user selection within a legal interval; never chooses a
     * midpoint/maximum automatically or treats an unknown minimum as statutory zero.
     * For CONSULTATION_HOUR, quantity is the exact number of hours. Otherwise it is one.
     */
    static Result ranged(Rules rules, String category, long selectedWholeRials, String quantity,
            Stage stage, boolean hasProsecutor, boolean finalCriminalTrial,
            String selectionBasis, RoundingMode rounding) {
        require(rules, stage, rounding);
        String basis = CalculationReference.required(selectionBasis, "مبنای انتخاب مبلغ و انطباق تعرفه");
        Range range = rules.ranges.get(category);
        if (range == null) throw invalid("این دسته تعرفه پشتیبانی نشده است");
        if (stage == Stage.CIVIL_CASSATION && !"FAMILY".equals(category) && !"NONFINANCIAL".equals(category))
            throw invalid("ماده ۱۶ را نمی‌توان به این مرجع تسری داد");
        if (selectedWholeRials < 0 || selectedWholeRials > range.maximum
                || range.minimum != null && selectedWholeRials < range.minimum)
            throw invalid("مبلغ انتخابی خارج از حدود تعرفه است");
        CalculationArithmetic.Fraction q = CalculationArithmetic.Fraction.decimal(CalculationArithmetic.decimal(quantity));
        if (q.numerator.signum() == 0) throw invalid("مقدار خدمت باید مثبت باشد");
        if (!"CONSULTATION_HOUR".equals(category) && !q.numerator.equals(q.denominator))
            throw invalid("تعداد یا ساعت فقط برای مشاوره ساعتی مجاز است");
        if (range.family != Family.CRIMINAL && (!hasProsecutor || finalCriminalTrial))
            throw invalid("گزینه‌های مرحله کیفری به این دسته تعلق ندارد");
        CalculationArithmetic.Fraction s = share(rules, range.family, stage, hasProsecutor, finalCriminalTrial);
        CalculationArithmetic.Fraction exact = CalculationArithmetic.Fraction.of(selectedWholeRials, 1).multiply(q).multiply(s);
        List<String> steps = new ArrayList<>();
        steps.add("ماده " + range.article + ": " + range.label);
        steps.add("مبلغ انتخابی مستند: " + selectedWholeRials + " ریال؛ مقدار خدمت " + q + "؛ سهم مرحله " + s);
        if (range.minimum == null) steps.add("این ماده فقط سقف دارد؛ حداقل قانونی در این بسته تعیین نشده است");
        return new Result(rules, range.article, stage, exact, basis, steps, rounding);
    }

    /** Article 25: an interval, NOT an automatic 2% fee. Selection must fit the
     * exact upper bound before rounding. Contradictory bounds require legal review. */
    static Result enforcement(Rules rules, long awardRials, long selectedRials,
            Stage stage, String applicabilityBasis, RoundingMode rounding) {
        require(rules, stage, rounding);
        String basis=CalculationReference.required(applicabilityBasis,"مبنای انتخاب مبلغ اجرای احکام");
        if(stage!=Stage.WHOLE)throw invalid("امور اجرایی در این مسیر خدمت مستقل است؛ مرحله کل را انتخاب کنید");
        if(rules.enforcementMinimum==null)throw invalid("بسته تاریخی فاقد قاعده اجرای احکام است");
        if(awardRials<=0)throw invalid("مبلغ محکوم‌به یا مورد اجرا باید مثبت باشد");
        CalculationArithmetic.Fraction upper=CalculationArithmetic.Fraction.of(awardRials,1).multiply(rules.enforcementMaximumRate);
        if(java.math.BigInteger.valueOf(rules.enforcementMinimum).multiply(upper.denominator).compareTo(upper.numerator)>0)
            throw invalid("سقف درصدی کمتر از حداقل تعرفه است؛ تعیین مبلغ نیازمند بررسی حقوقی مستقل است");
        if(selectedRials<rules.enforcementMinimum||java.math.BigInteger.valueOf(selectedRials).multiply(upper.denominator).compareTo(upper.numerator)>0)
            throw invalid("مبلغ منتخب خارج از حدود دقیق تعرفه اجرای احکام است");
        List<String> steps=new ArrayList<>();
        steps.add("ماده ۲۵: حداقل "+rules.enforcementMinimum+" ریال؛ سقف دقیق "+awardRials+" × "+rules.enforcementMaximumRate+" = "+upper+" ریال");
        steps.add("مبلغ منتخب مستند در بازه: "+selectedRials+" ریال؛ سقف به‌طور خودکار حق‌الوکاله تلقی نشده است");
        return new Result(rules,"25",stage,CalculationArithmetic.Fraction.of(selectedRials,1),basis,steps,rounding);
    }

    /** Article 12's four explicit timing branches. Do not infer an order from a generic dismissal.
     * Apply to the exact stage result before its final monetary rounding. */
    static Result disposition(Rules rules, Result base, String category, Disposition selected) {
        if(rules==null||base==null||selected==null)throw invalid("اطلاعات قرار کامل نیست");
        if(!rules.version.equals(base.ruleVersion))throw invalid("نسخه مبنا و قرار متفاوت است");
        if(selected==Disposition.ORDINARY)return base;
        if(!("FINANCIAL".equals(category)||"FAMILY".equals(category)||"NONFINANCIAL".equals(category)))
            throw invalid("این مسیر قرار فقط برای دعوای مالی، خانواده یا غیرمالی دادگاه حقوقی است");
        boolean appeal=selected==Disposition.APPEAL_DROP_BEFORE_DEFENSE||selected==Disposition.APPEAL_DROP_AFTER_DEFENSE;
        if(selected==Disposition.NONHEARING_OR_RETRIAL_REFUSAL) {
            if(base.stage!=Stage.FIRST&&base.stage!=Stage.APPEAL&&base.stage!=Stage.CIVIL_CASSATION)
                throw invalid("برای قرار بند پ ماده ۱۲ مرحله رسیدگی مشخص را انتخاب کنید");
        } else if(base.stage!=(appeal?Stage.APPEAL:Stage.FIRST))throw invalid("مرحله انتخابی با نوع قرار منطبق نیست");
        CalculationArithmetic.Fraction factor=rules.dispositions.get(selected);
        if(factor==null)throw invalid("بسته تاریخی فاقد قاعده این قرار است");
        CalculationArithmetic.Fraction exact=base.exactAmount;
        List<String> steps=new ArrayList<>(base.steps);
        steps.add("ماده ۱۲؛ "+selected.name()+": مبلغ دقیق مرحله "+exact+" × "+factor+"؛ گرد کردن فقط در پایان");
        return new Result(rules,base.article+"/12",base.stage,exact.multiply(factor),base.selectedBasis,steps,base.rounding);
    }

    /** Explicit independent special pathways. Eligibility is documented, never inferred.
     * Preserve the unrounded base; no compound charge increments or repeated stage split. */
    static Result special(Rules rules, Result base, String category, Special special,
            int count, String evidence, Disposition disposition) {
        if(rules==null||base==null||special==null||disposition==null)throw invalid("اطلاعات حالت ویژه کامل نیست");
        if(!rules.version.equals(base.ruleVersion))throw invalid("نسخه مبنا و حالت ویژه متفاوت است");
        if(special==Special.NONE) {
            if(count!=1)throw invalid("بدون حالت تعدد، تعداد باید یک باشد");
            return base;
        }
        String proof=CalculationReference.required(evidence,"مستند شرایط حالت ویژه");
        if(disposition!=Disposition.ORDINARY)throw invalid("ترکیب حالت ویژه با قرار در این مسیر بررسی نشده است");
        CalculationArithmetic.Fraction factor=rules.specialFactors.get(special);
        if(factor==null)throw invalid("بسته تاریخی فاقد این حالت ویژه است");
        Range range=rules.ranges.get(category);
        boolean litigation="FINANCIAL".equals(category)||(range!=null&&range.family!=Family.SERVICE);
        if(!litigation)throw invalid("این حالت ویژه برای خدمت مستقل قابل انتخاب نیست");
        String article;
        switch(special) {
            case SETTLEMENT:
                if(base.stage!=Stage.WHOLE||count!=1)throw invalid("سازش موضوع ماده ۲۳ نیازمند مرحله کل و تعداد یک است");
                article="23";break;
            case EQUAL_COUNSEL:
                if(count<2)throw invalid("تعداد وکلا باید دست‌کم دو باشد");
                factor=factor.multiply(CalculationArithmetic.Fraction.of(1,count));
                article="5";break;
            case MULTIPLE_CHARGES:
                if(range==null||range.family!=Family.CRIMINAL||count<2)
                    throw invalid("تعدد اتهام نیازمند دسته جرم اشد و دست‌کم دو اتهام است");
                factor=CalculationArithmetic.Fraction.of(1,1).add(factor.multiply(CalculationArithmetic.Fraction.of((long)count-1,1)));
                article="14-note3";break;
            case APPOINTED_AID:
                if(count!=1||range==null||range.minimum==null)
                    throw invalid("وکالت تسخیری یا معاضدتی نیازمند دسته دارای حداقل مصرح و تعداد یک است");
                // The UI builds the base from the statutory minimum; never double an
                // arbitrary selected fee. Verify the exact stage base here as well.
                CalculationArithmetic.Fraction minimumWhole=CalculationArithmetic.Fraction.of(range.minimum,1);
                boolean matches=false;
                if(range.family==Family.CIVIL) {
                    CalculationArithmetic.Fraction expected=minimumWhole.multiply(share(rules,range.family,base.stage,true,false));
                    matches=expected.toString().equals(base.exactAmount.toString());
                } else if(range.family==Family.CRIMINAL) {
                    // Stage availability is retained by ranged(); consider all valid
                    // assignments without inferring the case's procedural facts.
                    for(boolean prosecutor:new boolean[]{true,false})for(boolean finalTrial:new boolean[]{true,false}) {
                        try {
                            CalculationArithmetic.Fraction expected=minimumWhole.multiply(share(rules,range.family,base.stage,prosecutor,finalTrial));
                            if(expected.toString().equals(base.exactAmount.toString()))matches=true;
                        }catch(IllegalArgumentException ignored){}
                    }
                }
                if(!matches)throw invalid("مبنای وکالت تسخیری یا معاضدتی باید حداقل تعرفه همان مرحله باشد");
                article="8";break;
            case CONTINUED_AFTER_REVERSAL:
                if(count!=1||range==null||range.family!=Family.CRIMINAL||
                        (base.stage!=Stage.FIRST&&base.stage!=Stage.APPEAL))
                    throw invalid("ادامه وکالت پس از نقض نیازمند دسته کیفری و مرحله بدوی یا تجدیدنظر است");
                article="14-note2";break;
            case CERTIFIED_SPECIALTY:
                if(count!=1)throw invalid("در تخصص تعداد باید یک باشد");
                factor=CalculationArithmetic.Fraction.of(1,1).add(factor);
                article="22-note";break;
            default:throw invalid("حالت ویژه ناشناخته است");
        }
        List<String> steps=new ArrayList<>(base.steps);
        steps.add("حالت ویژه "+special+"؛ ماده "+article+"؛ مبنای مستند: "+proof);
        steps.add("مبلغ دقیق مبنا "+base.exactAmount+" × "+factor+"؛ گرد کردن فقط در پایان");
        if(special==Special.EQUAL_COUNSEL)steps.add("نتیجه سهم هر وکیل است؛ بدون قرارداد حق‌الوکاله و بدون توافق متفاوت در تقسیم. کسر دقیق، مبنای تقسیم است؛ جمع ارقام گرد‌شده ممکن است اختلاف جزئی داشته باشد.");
        if(special==Special.MULTIPLE_CHARGES)steps.add("مبلغ مبنا تعرفه جرم اشد؛ افزایش برای هر اتهام اضافه نسبت به همان مبنا و غیرمرکب است.");
        if(special==Special.CONTINUED_AFTER_REVERSAL)steps.add("فقط حق‌الوکاله مرحله جدید پس از قبول فرجام یا اعاده دادرسی، نقض رأی و اعاده پرونده؛ حق‌الوکاله سابق به این نتیجه اضافه نشده و این مسیر وکیل جدید ماده ۱۸ نیست.");
        return new Result(rules,base.article+"/"+article,base.stage,base.exactAmount.multiply(factor),base.selectedBasis,steps,base.rounding);
    }

    /** Explicitly documented article-18 base, not an inferred contract or whole-case fee. */
    static Result replacementAfterReversal(Rules rules, long priorTariff, Stage stage,
            String basis, String evidence, RoundingMode rounding) {
        if(rules==null||rules.replacementRate==null)throw invalid("بسته تاریخی فاقد قاعده ماده ۱۸ است");
        if(priorTariff<=0||rounding==null)throw invalid("حق‌الوکاله مستند پیش از نقض باید مثبت باشد");
        if(stage!=Stage.FIRST&&stage!=Stage.APPEAL)throw invalid("مرحله رسیدگی پس از نقض را بدوی یا تجدیدنظر انتخاب کنید");
        String explanation=CalculationReference.required(basis,"مبنای تعیین حق‌الوکاله پیش از نقض");
        String proof=CalculationReference.required(evidence,"مستند نقض رأی و قبول وکالت توسط وکیل جدید");
        List<String> steps=new ArrayList<>();
        steps.add("ماده ۱۸؛ حق‌الوکاله تعرفه‌ای مستند پیش از نقض: "+priorTariff+" ریال؛ "+explanation);
        steps.add("مستند نقض و وکیل جدید: "+proof);
        steps.add("مبلغ پیش از نقض × "+rules.replacementRate+"؛ بدون تقسیم دوباره سهم مرحله؛ گرد کردن فقط در پایان");
        steps.add("فقط حق‌الوکاله وکیل جدید؛ مبلغ قرارداد قبلی یا جمع کل مراحل، خودکار مبنا نیست. با تبصره ۲ ماده ۱۴ جمع نشده است.");
        return new Result(rules,"18",stage,CalculationArithmetic.Fraction.of(priorTariff,1).multiply(rules.replacementRate),explanation,steps,rounding);
    }

    static long agreedFee(String amount, CalculationArithmetic.Currency currency) {
        // Independent contractual input; never substitutes the tariff or modifies a contract.
        return CalculationArithmetic.money(amount, currency);
    }

    private static CalculationArithmetic.Fraction share(Rules r, Family family, Stage stage,
            boolean prosecutor, boolean finalTrial) {
        if (family == Family.SERVICE) {
            if (stage != Stage.WHOLE) throw invalid("خدمت مستقل سهم مرحله دادرسی ندارد");
            return CalculationArithmetic.Fraction.of(1, 1);
        }
        if (family == Family.CIVIL) {
            switch (stage) {
                case WHOLE: return CalculationArithmetic.Fraction.of(1, 1);
                case FIRST: return r.civilFirst;
                case APPEAL: case CIVIL_CASSATION: return r.civilAppeal;
                default: throw invalid("مرحله دادسرا برای تعرفه مدنی معتبر نیست");
            }
        }
        if (stage == Stage.WHOLE) return CalculationArithmetic.Fraction.of(1, 1);
        if (stage == Stage.PROSECUTOR && prosecutor) return r.criminalProsecutor;
        if (stage == Stage.APPEAL && !finalTrial) return r.criminalAppeal;
        if (stage == Stage.FIRST) {
            CalculationArithmetic.Fraction part = r.criminalFirst;
            if (!prosecutor) part = part.add(r.criminalProsecutor);
            if (finalTrial) part = part.add(r.criminalAppeal);
            return part;
        }
        throw invalid("مرحله انتخابی با مسیر کیفری پرونده سازگار نیست");
    }

    private static void require(Rules rules, Stage stage, RoundingMode rounding) {
        if (rules == null || stage == null || rounding == null) throw invalid("قواعد، مرحله و روش گرد کردن الزامی است");
    }
    private static IllegalArgumentException invalid(String message) { return new IllegalArgumentException(message); }
}
