package ir.kamranvahdati.lawoffice;
import java.io.*;
import java.util.*;
/** Direct CBI transcription, base 1400. Never rebases or combines different baskets. */
final class CalculationCurrentIndices {
 static final String VERSION="CBI-base1400-through1405-06/1";
 static final String SERIES="CBI-CPI-urban-base1400-monthly";
 static final String URL="https://cbi.ir/page/36186.aspx";
 static final String SHA256="8042052dbb7436850028be5e599476f756b50e1e387d7e4f71ccb68e5ac73ebb";
 final List<CalculationReference> references;
 CalculationCurrentIndices(Reader reader)throws IOException{
  Properties p=new Properties();p.load(reader);if(p.size()!=6)throw new IllegalArgumentException("بسته شاخص رسمی ناقص است");
  List<CalculationReference> all=new ArrayList<>();
  for(int year=1400;year<=1405;year++){
   String[] cells=p.getProperty(Integer.toString(year),"").split(",",-1);int months=year==1405?6:12;
   if(cells.length!=months)throw new IllegalArgumentException("پوشش ماه‌های بسته رسمی صحیح نیست");
   for(int m=1;m<=months;m++)all.add(new CalculationReference(VERSION+"/"+year+"/"+m,SERIES,VERSION,"",CalculationReference.Kind.ECONOMIC_INDEX,year,m,
    new JalaliDate(year,m,1).value(),new JalaliDate(year,m,JalaliDate.monthLength(year,m)).value(),cells[m-1],"INDEX-1400=100",
    "بانک مرکزی؛ شاخص کل بهای کالاها و خدمات مصرفی در مناطق شهری؛ گزارش شهریور ۱۴۰۵","PDF دریافت‌شده مستقیم از بانک مرکزی؛ صفحات چاپی ۵ و ۶","","1405/06/31",URL,
    "تاریخ منبع، پایان دوره گزارش است؛ تاریخ دقیق انتشار در سند مشخص نیست. سال پایه و سبد ۱۴۰۰؛ با سبد ۱۳۹۵ مخلوط یا تبدیل نشده است. ستون شاخص، نه درصد تورم. SHA256: "+SHA256,
    CalculationReference.Status.OFFICIAL_VERIFIED,1791460800000L,1791460800000L,false,false,false));
  }references=Collections.unmodifiableList(all);
 }
 CalculationReference at(String date){JalaliDate d=JalaliDate.parse(date);
  if(d.year<1400||d.year>1405||d.year==1405&&d.month>6)throw new IllegalArgumentException("شاخص رسمی این بسته فقط از فروردین ۱۴۰۰ تا شهریور ۱۴۰۵ موجود است؛ ماه دیگر تخمین زده نمی‌شود");
  return CalculationReference.select(references,CalculationReference.Kind.ECONOMIC_INDEX,SERIES,d.year,d.month,d.value());
 }
}
