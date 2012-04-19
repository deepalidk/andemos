require('luacom')
require('luasql.sqlite3')

local EXCEL_DIRECOTORY="D:\\android\\android\\workspace\\data\\CodeLibrary_20120216\\"
local EXCEL_TYPE_FILENAME="KeyID_DeviceID_US_for LC_20120216.xls"
local EXCEL_CODELIST_FILENAME="Codelist_LC_US_v11_20120215_(for release).xls"
local EXCEL_UIRDDATA_FILENAME="US_LIBRARY_for_LC_Full_device_20111222.xls"

local DB_PATH="D:\\android\\android\\workspace\\UniversalRemote\\res\\raw\\codelib.db"

local CODELIST_SHEETS={'TV','VCR','SAT','CBL','DVD','AUDIO','CD','HOME AUTOMATION'}
local UIRD_SHEETS={'TV','VCR','SAT','CTV','DVD','Aud','CD','HOME'}

--新建Excel文件
function _ExcelBookNew(Visible)
local oExcel = luacom.CreateObject("Excel.Application")
if oExcel == nil then error("Object is not create") end
--处理是否可见
if tonumber(Visible) == nil then error("Visible is not a number") end
if Visible == nil then Visible = 1 end
if Visible > 1 then Visible = 1 end
if Visible < 0 then Visible = 0 end

oExcel.Visible = Visible
oExcel.WorkBooks:Add()
oExcel.ActiveWorkbook.Sheets(1):Select()
return oExcel
end
--打开已有的Excel文件
function _ExcelBookOpen(FilePath,Visible,ReadOnly)
local oExcel = luacom.CreateObject("Excel.Application")
if oExcel == nil then error("Object is not create") end
--查看文件是否存在
local t=io.open(FilePath,"r")
if t == nil then
--文件不存在时的处理
oExcel.Application:quit()
oExcel=nil
error("File is not exists")
else
t:close()
end
--处理是否可见ReadOnly
if Visible == nil then Visible = 1 end
if tonumber(Visible) == nil then error("Visible is not a number") end
if Visible > 1 then Visible = 1 end
if Visible < 0 then Visible = 0 end
--处理是否只读
if ReadOnly == nil then ReadOnly = 0 end
if tonumber(ReadOnly) == nil then error("ReadOnly is not a number") end
if ReadOnly > 1 then ReadOnly = 1 end
if ReadOnly < 0 then ReadOnly = 0 end
oExcel.Visible = Visible
--打开指定文件
oExcel.WorkBooks:Open(FilePath,nil,ReadOnly)
oExcel.ActiveWorkbook.Sheets(1):Select()
return oExcel
end
--写入Cells数据
function _ExcelWriteCell(oExcel,Value,Row,Column)
--验证参数
if oExcel == nil then error("oExcel is not a object!") end
if tonumber(Row) == nil or Row < 1 then error("Row is not a valid number!") end
if tonumber(Column) == nil or Column < 1 then error("Column is not a valid number!") end
--对指定Cell位置赋值
oExcel.Activesheet.Cells(Row, Column).Value2 = Value
return 1
end
--读取Cells数据
function _ExcelReadCell(oExcel,Row,Column)
--验证参数
if oExcel == nil then error("oExcel is not a object!") end
if tonumber(Row) == nil or Row < 1 then error("Row is not a valid number!") end
if tonumber(Column) == nil or Column < 1 then error("returnColumn is not a valid number!") end
--返回指定Cell位置值
return oExcel.Activesheet.Cells(Row, Column).Value2
end
--保存Excel文件
function _ExcelBookSave(oExcel, Alerts)
--验证参数
if oExcel == nil then error("oExcel is not a object!") end
--处理是否提示
if Alerts == nil then Alerts = 0 end
if tonumber(Alerts) == nil then error("Alerts is not a number") end
if Alerts > 1 then Alerts = 1 end
if Alerts < 0 then Alerts = 0 end
oExcel.Application.DisplayAlerts = Alerts
oExcel.Application.ScreenUpdating = Alerts
--进行保存
oExcel.ActiveWorkBook:Save()
if not Alerts then
oExcel.Application.DisplayAlerts = 1
oExcel.Application.ScreenUpdating = 1
end
return 1
end
--另存Excel文件
function _ExcelBookSaveAs(oExcel,FilePath,Type,Alerts,OverWrite)
--验证参数
if oExcel == nil then error("oExcel is not a object!") end
--处理保存文件类型
if Type == nil then Type = "xls" end
if Type == "xls" or Type == "csv" or Type == "txt" or Type == "template" or Type == "html" then
if Type == "xls" then Type = -4143 end -- xlWorkbookNormal
if Type == "csv" then Type = 6 end -- xlCSV
if Type == "txt" then Type = -4158 end -- xlCurrentPlatformText
if Type == "template" then Type = 17 end -- xlTemplate
if Type == "html" then Type = 44 end -- xlHtml
else
error("Type is not a valid type")
end
--处理是否提示
if Alerts == nil then Alerts = 0 end
if tonumber(Alerts) == nil then error("Alerts is not a number") end
if Alerts > 1 then Alerts = 1 end
if Alerts < 0 then Alerts = 0 end
oExcel.Application.DisplayAlerts = Alerts
oExcel.Application.ScreenUpdating = Alerts
--处理文件是否OverWrite
if OverWrite == nil then OverWrite = 0 end
--查看文件是否存在
local t=io.open(FilePath,"r")
--如果文件存在且OverWrite参数为0，返回错误
if not t == nil then
if not OverWrite then
t:close()
error("Can't overwrite the file!")
end
t:close()
os.remove(FilePath)
end
--保存文件
if FilePath == nil then error("FilePath is not valid !") end
--使用ActiveWorkBook时，在已经打开文件时，无法另存，所以使用WorkBookS(1)进行处理
oExcel.WorkBookS(1):SaveAs(FilePath,Type)
--继续处理Alerts参数，以便继续使用
if not Alerts then
oExcel.Application.DisplayAlerts = 1
oExcel.Application.ScreenUpdating = 1
end
return 1
end
--关闭Excel文件
function _ExcelBookClose(oExcel,Save,Alerts)
--验证参数
if oExcel == nil then error("oExcel is not a object!") end
--处理是否保存
if Save == nil then Save = 1 end
if tonumber(Save) == nil then error("Save is not a number") end
if Save > 1 then Save = 1 end
if Save < 0 then Save = 0 end
--处理是否提示
if Alerts == nil then Alerts = 0 end
if tonumber(Alerts) == nil then error("Alerts is not a number") end
if Alerts > 1 then Alerts = 1 end
if Alerts < 0 then Alerts = 0 end

if Save == 1 then oExcel.ActiveWorkBook:save() end
oExcel.Application.DisplayAlerts = Alerts
oExcel.Application.ScreenUpdating = Alerts
oExcel.Application:Quit()
return 1
end
--列出所有Sheet
function _ExcelSheetList(oExcel)
--验证参数
if oExcel == nil then error("oExcel is not a object!") end
local temp = oExcel.ActiveWorkbook.Sheets.Count
local tab = {}
tab[0] = temp
for i = 1,temp do
tab[i] = oExcel.ActiveWorkbook.Sheets(i).Name
end
--返回一个table，其中tab[0]为个数
return tab
end
--激活指定的sheet
function _ExcelSheetActivate(oExcel, vSheet)
local tab = {}
local found = 0
--验证参数
if oExcel == nil then error("oExcel is not a object!") end
--设置默认sheet为1
if vSheet == nil then vSheet = 1 end
--如果提供参数为数字
if tonumber(vSheet) ~= nil then
if oExcel.ActiveWorkbook.Sheets.Count < tonumber(vSheet) then error("The sheet value is to biger!") end
--如果提供参数为字符
else
tab = _ExcelSheetList(oExcel)
for i = 1 , tab[0] do
if tab[i] == vSheet then found = 1 end
end
if found ~= 1 then error("Can't find the sheet") end
end
oExcel.ActiveWorkbook.Sheets(vSheet):Select ()
return 1
end




--参数基本做到了可以省略
--b=assert(_ExcelBookOpen("c:\\d.xls"))
--assert(_ExcelSheetActivate(b))
--assert(_ExcelWriteCell(b,"hello lua!",1,1))
--assert(_ExcelBookSave(b,1))
--assert(_ExcelBookClose(b))

--b=assert(_ExcelBookNew(1))
--tab=assert(_ExcelSheetList(b))
--for i,v in pairs(tab) do
--print(i,v)
--end
--assert(_ExcelSheetActivate(b,"Sheet2"))
--b=assert(_ExcelBookOpen("c:\\d.xls",1,0))
--assert(_ExcelWriteCell(b,"haha",1,1))
--assert(_ExcelBookSaveAs(b,"c:\\a","txt",0,0))
--print(_ExcelReadCell(b,1,1))
--assert(_ExcelBookClose(b))

env = assert(luasql.sqlite3())
db = assert(env:connect(DB_PATH))
db:setautocommit(false)

--import the category table.
oExcel=assert(_ExcelBookOpen(EXCEL_DIRECOTORY..EXCEL_TYPE_FILENAME,1,1))
assert(_ExcelSheetActivate(oExcel))


assert(_ExcelSheetActivate(oExcel,"DEVICE NO. LIST"))

rowCount=oExcel.ActiveWorkbook.Activesheet.UsedRange.Rows.Count

local  sql
sql=string.format("DELETE FROM category")
res = assert(db:execute(sql))

for row=2,rowCount do

  devType=oExcel.ActiveWorkbook.Activesheet.Cells(row, 1).Value2
  name=oExcel.ActiveWorkbook.Activesheet.Cells(row, 2).Value2
  if nil~= devType and nil~= name then
  print(EXCEL_TYPE_FILENAME.." DEVICE NO. LIST ".. ".Row:"..row)
  sql=string.format("INSERT INTO category VALUES('%s','%s')",devType,name)
  res = assert(db:execute(sql))
  end
end

assert(db:commit())

assert(_ExcelBookClose(oExcel,0))

--import the cate gory table end.

--import the codelist table.
oExcel=assert(_ExcelBookOpen(EXCEL_DIRECOTORY..EXCEL_CODELIST_FILENAME,1,1))
assert(_ExcelSheetActivate(oExcel))

sql=string.format("DELETE FROM irCodeList where irCodeSrc=2")
res = assert(db:execute(sql))

for k,sheetName in pairs(CODELIST_SHEETS) do

assert(_ExcelSheetActivate(oExcel,sheetName))

rowCount=oExcel.ActiveWorkbook.Activesheet.UsedRange.Rows.Count

for row=2,rowCount do

  devType=oExcel.ActiveWorkbook.Activesheet.Cells(row, 2).Value2
  brand=oExcel.ActiveWorkbook.Activesheet.Cells(row, 3).Value2
  codeNum=oExcel.ActiveWorkbook.Activesheet.Cells(row, 4).Value2

  if nil~= brand then
  print(EXCEL_CODELIST_FILENAME.."."..sheetName.. ".Row:"..row)
  sql=string.format("INSERT INTO irCodeList('devType','brandName','irCodeSrc','irCodeNum') VALUES('%s','%s','2','%s')",devType,brand,codeNum)
  res = assert(db:execute(sql))
  end
end

end

assert(db:commit())

assert(_ExcelBookClose(oExcel,0))

--import the codelist table end.


--import the uird data table.
oExcel=assert(_ExcelBookOpen(EXCEL_DIRECOTORY..EXCEL_UIRDDATA_FILENAME,1,1))
assert(_ExcelSheetActivate(oExcel))

sql=string.format("drop table tbUirdData")
db:execute(sql)

sql=string.format("create table tbUirdData(codeNum integer,devType integer,keyId integer,data text)")
res = assert(db:execute(sql))


for k,sheetName in pairs(UIRD_SHEETS) do

assert(_ExcelSheetActivate(oExcel,sheetName))

rowCount=oExcel.ActiveWorkbook.Activesheet.UsedRange.Rows.Count

for row=3,rowCount do
  codeNum=oExcel.ActiveWorkbook.Activesheet.Cells(row, 2).Value2
  devType=oExcel.ActiveWorkbook.Activesheet.Cells(row, 3).Value2
  keyId=oExcel.ActiveWorkbook.Activesheet.Cells(row, 4).Value2
  irData=oExcel.ActiveWorkbook.Activesheet.Cells(row, 5).Value2

  if nil~= irData then
  print(EXCEL_UIRDDATA_FILENAME.."."..sheetName.. ".Row:"..row)
  sql=string.format("INSERT INTO tbUirdData('codeNum','devType','keyId','data') VALUES('%s','%s','%d','%s')",codeNum,devType,tonumber(keyId,16),irData)
  res = assert(db:execute(sql))
  end
end

end

assert(db:commit())

assert(_ExcelBookClose(oExcel,0))


--import the uird data table end.

env:close()


