import fs from 'node:fs/promises';
import {createRequire} from 'node:module';
import {pathToFileURL} from 'node:url';
const require=createRequire('C:/Users/hp/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json');
const {Workbook,SpreadsheetFile}=await import(pathToFileURL(require.resolve('@oai/artifact-tool')).href);
const wb=Workbook.create();
const sh=wb.worksheets.add('المنيو');
const beef='100 جم برجر سماش، جبنة شيدر، خس، خيار مخلل، كاتشب، مستردة. مع فرايز.';
const chicken='150 جم صدور فراخ، خلطة الناشفيل الحارة، كول سلو. مع فرايز. أوريجنال أو سبايسي.';
const rows=[
['سماش برجر سنجل + فرايز',105,'سماش برجر',beef,''],
['سماش برجر دبل + فرايز',155,'سماش برجر',beef,''],
['سماش برجر تريبل + فرايز',205,'سماش برجر',beef,''],
['برجر تشيكن ناشفيل سنجل + فرايز',110,'برجر تشيكن ناشفيل',chicken,''],
['برجر تشيكن ناشفيل دبل + فرايز',160,'برجر تشيكن ناشفيل',chicken,''],
['برجر تشيكن ناشفيل تريبل + فرايز',205,'برجر تشيكن ناشفيل',chicken,''],
['بطاطس',35,'بطاطس','',''],
['بطاطس بالجبن الشيدر',50,'بطاطس','',''],
['بطاطس بالجبن الشيدر وقطع الهالبينو',55,'بطاطس','',''],
['لوديد بيف فرايز - السعر الأول',80,'بطاطس','بطاطس بالجبن الشيدر وقطعة برجر سماش.','يوجد سعران 80 و150؛ الحجم غير موضح. راجع الاسم قبل الاستيراد.'],
['لوديد بيف فرايز - السعر الثاني',150,'بطاطس','بطاطس بالجبن الشيدر وقطعة برجر سماش.','يوجد سعران 80 و150؛ الحجم غير موضح. راجع الاسم قبل الاستيراد.'],
['لوديد تشيكن فرايز - السعر الأول',80,'بطاطس','بطاطس بالجبن الشيدر وقطعة برجر تشيكن.','يوجد سعران 80 و150؛ الحجم غير موضح. راجع الاسم قبل الاستيراد.'],
['لوديد تشيكن فرايز - السعر الثاني',150,'بطاطس','بطاطس بالجبن الشيدر وقطعة برجر تشيكن.','يوجد سعران 80 و150؛ الحجم غير موضح. راجع الاسم قبل الاستيراد.'],
['كاتشب',15,'الإضافات','',''],
['صوص رانش',20,'الإضافات','',''],
['صوص باربيكيو',20,'الإضافات','',''],
['صوص ناشفيل',25,'الإضافات','',''],
['صوص شيدر',20,'الإضافات','',''],
['صوص تكساس',20,'الإضافات','',''],
['هالبينو',15,'الإضافات','',''],
['خيار',10,'الإضافات','',''],
['كول سلو - السعر الأول',20,'الإضافات','','يوجد سعران 20 و40؛ الحجم غير موضح. راجع الاسم قبل الاستيراد.'],
['كول سلو - السعر الثاني',40,'الإضافات','','يوجد سعران 20 و40؛ الحجم غير موضح. راجع الاسم قبل الاستيراد.'],
['مشروب (في كولا)',25,'الإضافات','','']
];
sh.getRange('A1:F25').format.font={name:'Arial',size:11};
sh.getRange('A1:F25').format.verticalAlignment='center';
sh.getRange('A1:F25').format.rowHeight=32;
sh.getRange('A1:D1').values=[['الاسم','السعر بالجنيه','القسم','الوصف']];
sh.getRange('A2:D25').values=rows.map(r=>r.slice(0,4));
sh.getRange('F1').values=[['ملاحظات المراجعة']];
sh.getRange('F2:F25').values=rows.map(r=>[r[4]]);
sh.getRange('H1').values=[['المصدر: صورة المنيو المرفقة']];
sh.getRange('H3').values=[['للاستيراد: انسخ A2:D25 بدون العناوين.']];
sh.getRange('H5').values=[['راجع الصفوف الصفراء قبل الاستيراد.']];
sh.getRange('H7').values=[['الإضافات محفوظة كأصناف منفصلة حالياً.']];
sh.getRange('A1:D1').format={fill:'#813D29',font:{name:'Arial',size:11,bold:true,color:'#FFFFFF'}};
sh.getRange('A1:D1').format.horizontalAlignment='center';
sh.getRange('A1:A25').format.columnWidth=43;
sh.getRange('B1:B25').format.columnWidth=16;
sh.getRange('B2:B25').setNumberFormat('0');
sh.getRange('C1:C25').format.columnWidth=25;
sh.getRange('D1:D25').format.columnWidth=65;
sh.getRange('D2:D25').format.wrapText=true;
sh.getRange('A2:D7').format.rowHeight=56;
sh.getRange('E1:E25').format.columnWidth=3;
sh.getRange('F1:F25').format.columnWidth=84;
sh.getRange('H1:H7').format.columnWidth=49;
sh.getRange('H1:H7').format.font={name:'Arial',size:11};
for(let i=0;i<rows.length;i++) {
 if(rows[i][4]) sh.getRange(`A${i+2}:D${i+2}`).format.fill='#FFF0C2';
 else if(i%2===1) sh.getRange(`A${i+2}:D${i+2}`).format.fill='#F7F1ED';
}
sh.freezePanes.freezeRows(1);sh.showGridLines=false;
wb.recalculate();
console.log((await wb.inspect({kind:'table',range:'المنيو!A1:D8',include:'values',tableMaxRows:8,tableMaxCols:4,maxChars:2200})).ndjson);
const dir='D:/projects/Kapinda/outputs/menu-photo';
await (await SpreadsheetFile.exportXlsx(wb)).save(`${dir}/menu-from-photo.xlsx`);
const preview=await wb.render({sheetName:'المنيو',range:'A1:D13',scale:1.4,format:'png'});
await fs.writeFile(`${dir}/preview.png`,new Uint8Array(await preview.arrayBuffer()));
console.log('Exported 24 items');
