package com.hanqian.kepler.web.controller.question;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.hanqian.kepler.common.bean.NameValueVo;
import com.hanqian.kepler.common.bean.jqgrid.JqGridReturn;
import com.hanqian.kepler.common.bean.result.AjaxResult;
import com.hanqian.kepler.common.enums.BaseEnumManager;
import com.hanqian.kepler.common.jpa.specification.Rule;
import com.hanqian.kepler.common.jpa.specification.SpecificationFactory;
import com.hanqian.kepler.common.utils.ExcelUtils;
import com.hanqian.kepler.core.entity.primary.question.Question;
import com.hanqian.kepler.core.service.question.QuestionService;
import com.hanqian.kepler.core.vo.QuestionCountVo;
import com.hanqian.kepler.core.vo.QuestionEchartVo;
import com.hanqian.kepler.core.vo.QuestionExportVo;
import com.hanqian.kepler.core.vo.QuestionSearchVo;
import com.hanqian.kepler.web.controller.BaseController;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/question")
public class QuestionController extends BaseController {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${kepler.questionLogMaxCount}")
    private Integer questionLogMaxCount;

    @Autowired
    private QuestionService questionService;

    /**
     * ??????????????? - ??????????????????
     */
    @CrossOrigin
    @GetMapping("save")
    @ResponseBody
    public AjaxResult save(Question question){
        logger.info(StrUtil.format("READY SAVE ->???{}???", JSONUtil.toJsonStr(question)));

        if(StrUtil.isBlank(question.getHospitalName())) return AjaxResult.error("???????????????????????????hospitalName???");
        if(ObjectUtil.isNull(question.getObjectType())) return AjaxResult.error("???????????????????????????ObjectType???");
        if(ObjectUtil.isNull(question.getSex())) return AjaxResult.error("?????????????????????sex???");
        if(ObjectUtil.isNull(question.getAgeField())) return AjaxResult.error("????????????????????????ageField???");

        if(questionService.getCountByObjectAndHospitalName(question.getHospitalName(), question.getObjectType())>=questionLogMaxCount) return AjaxResult.error("?????????????????????");

        // ======== ?????????????????? ========
        if(ObjectUtil.isNull(question.getQualityIndoor())) return AjaxResult.error("?????????????????? ?????????qualityIndoor???");
        if(ObjectUtil.isNull(question.getQualityOutdoor())) return AjaxResult.error("?????????????????? ?????????qualityOutdoor???");
        if(ObjectUtil.isNull(question.getToiletHygiene())) return AjaxResult.error("?????????????????? ?????????toiletHygiene???");
        if(ObjectUtil.isNull(question.getCleanService())) return AjaxResult.error("?????????????????? ?????????cleanService???");

        // ======== ?????????????????? ========
        if(ObjectUtil.isNull(question.getDailySecurity())) return AjaxResult.error("?????????????????? ?????????dailySecurity???");
        if(ObjectUtil.isNull(question.getAccidentalDisposal())) return AjaxResult.error("????????????????????? ?????????accidentalDisposal???");
        if(ObjectUtil.isNull(question.getSecurityService())) return AjaxResult.error("?????????????????? ?????????securityService???");

        // ======== ???????????????????????????????????? ========
        if(StrUtil.equalsAny(question.getObjectType().name(), new String[]{BaseEnumManager.ObjectTypeEnum.Patient.name(), BaseEnumManager.ObjectTypeEnum.PatientFamily.name()})){
	        if(ObjectUtil.isNull(question.getDeliveryTimeliness())) return AjaxResult.error("?????????????????? ?????????deliveryTimeliness???");
	        if(ObjectUtil.isNull(question.getFoodNutrition())) return AjaxResult.error("?????????????????? ?????????foodNutrition???");
	        if(ObjectUtil.isNull(question.getDiningAttitude())) return AjaxResult.error("?????????????????? ?????????diningAttitude???");
        }

        // ======== ????????????????????????????????????????????? ========
        if(StrUtil.equalsAny(question.getObjectType().name(), new String[]{BaseEnumManager.ObjectTypeEnum.Doctor.name(), BaseEnumManager.ObjectTypeEnum.Nurse.name(), BaseEnumManager.ObjectTypeEnum.Other.name()})){
	        if(ObjectUtil.isNull(question.getDishPrice())) return AjaxResult.error("???????????? ?????????dishPrice???");
	        if(ObjectUtil.isNull(question.getDiningEnvironment())) return AjaxResult.error("???????????? ?????????diningEnvironment???");
	        if(ObjectUtil.isNull(question.getFoodService())) return AjaxResult.error("?????????????????? ?????????foodService???");
        }

        // ======== ?????????????????? ========
        if(ObjectUtil.isNull(question.getTransportTimeliness())) return AjaxResult.error("??????????????? ?????????transportTimeliness???");
        if(ObjectUtil.isNull(question.getTransportAccuracy())) return AjaxResult.error("??????????????? ?????????transportAccuracy???");
        if(ObjectUtil.isNull(question.getTransportService())) return AjaxResult.error("?????????????????? ?????????transportService???");

        // ======== ?????????????????? ========
        if(ObjectUtil.isNull(question.getRepairTimeliness())) return AjaxResult.error("??????????????? ?????????repairTimeliness???");
        if(ObjectUtil.isNull(question.getRepairQuality())) return AjaxResult.error("???????????? ?????????repairQuality???");
        if(ObjectUtil.isNull(question.getElevatorStatus())) return AjaxResult.error("??????????????? ?????????elevatorStatus???");
        if(ObjectUtil.isNull(question.getOperationService())) return AjaxResult.error("?????????????????? ?????????operationService???");

        question.setObjectTypeSort(question.getObjectType().key());
        question = questionService.save(question);
        logger.info(StrUtil.format("SAVE->???{}???", JSONUtil.toJsonStr(question)));
        return AjaxResult.success("????????????");
    }

    /**
     * ??????????????? - ????????????????????????????????????????????????????????????????????????
     */
    @CrossOrigin
    @GetMapping("checkCount")
    @ResponseBody
    public AjaxResult checkCount(String hospitalName, String objectType){
        if(StrUtil.isBlank(hospitalName)) return AjaxResult.error("???????????????????????? ?????? ???hospitalName???");
        if(StrUtil.isBlank(objectType)) return AjaxResult.error("???????????????????????? ?????? ???objectType???");
        BaseEnumManager.ObjectTypeEnum objectTypeEnum = null;
        try{
            objectTypeEnum = BaseEnumManager.ObjectTypeEnum.valueOf(objectType);
        }catch (Exception e){
            return AjaxResult.error("???????????????????????????????????????objectType???");
        }
        return questionService.getCountByObjectAndHospitalName(hospitalName, objectTypeEnum)<questionLogMaxCount ? AjaxResult.success("????????????") : AjaxResult.warn("?????????????????????");
    }


    //???????????????????????????
    private List<NameValueVo> findHospitalNameListCommon(){
        List<String> names = EnumUtil.getNames(BaseEnumManager.HospitalName.class);
        List<NameValueVo> nameValueVoList = new ArrayList<>();
        names.forEach(name->{
            BaseEnumManager.HospitalName hospitalName = BaseEnumManager.HospitalName.valueOf(name);
            NameValueVo nameValueVo = new NameValueVo(hospitalName.name(), hospitalName.value());
            nameValueVoList.add(nameValueVo);
        });
        return nameValueVoList;
    }

    /**
     * ??????????????????????????????
     */
    @GetMapping("findHospitalNameList")
    @ResponseBody
    public AjaxResult findHospitalNameList(){
        return AjaxResult.success("????????????", findHospitalNameListCommon());
    }

    /**
     * ???????????????????????????_????????????
     */
    @GetMapping("bar_data_objectType")
    @ResponseBody
    public QuestionEchartVo bar_data_objectType(QuestionSearchVo questionSearch){
        List<Object[]> objects = questionService.findGroupData("objectType", questionSearch);
        String[] xList = new String[objects.size()];
        BigDecimal[] yList = new BigDecimal[objects.size()];
        for(int i=0;i<objects.size();i++){
            Object[] oArr = objects.get(i);
            String name = String.valueOf(oArr[0]);
            try {
                xList[i] = BaseEnumManager.ObjectTypeEnum.valueOf(name).value();
            }catch (Exception e){
                xList[i] = name;
            }

            yList[i] = NumberUtil.toBigDecimal(String.valueOf(oArr[1]));
        }
        return new QuestionEchartVo(xList, yList);
    }

    /**
     * ???????????????????????????_??????
     */
    @GetMapping("bar_data_sex")
    @ResponseBody
    public QuestionEchartVo bar_data_sex(QuestionSearchVo questionSearch){
        List<Object[]> objects = questionService.findGroupData("sex", questionSearch);
        String[] xList = new String[objects.size()];
        BigDecimal[] yList = new BigDecimal[objects.size()];
        for(int i=0;i<objects.size();i++){
            Object[] oArr = objects.get(i);
            String name = String.valueOf(oArr[0]);
            try {
                xList[i] = BaseEnumManager.SexEnum.valueOf(name).value();
            }catch (Exception e){
                xList[i] = name;
            }

            yList[i] = NumberUtil.toBigDecimal(String.valueOf(oArr[1]));
        }
        return new QuestionEchartVo(xList, yList);
    }

    /**
     * ???????????????????????????_?????????
     */
    @GetMapping("bar_data_ageField")
    @ResponseBody
    public QuestionEchartVo bar_data_ageField(QuestionSearchVo questionSearch){
        List<Object[]> objects = questionService.findGroupData("ageField", questionSearch);
        String[] xList = new String[objects.size()];
        BigDecimal[] yList = new BigDecimal[objects.size()];
        for(int i=0;i<objects.size();i++){
            Object[] oArr = objects.get(i);
            String name = String.valueOf(oArr[0]);
            switch (name){
                case "1": xList[i] = "18?????????"; break;
                case "2": xList[i] = "19~40"; break;
                case "4": xList[i] = "41~60"; break;
                case "6": xList[i] = "61?????????"; break;
                default: xList[i] = "??????";
            }
            yList[i] = NumberUtil.toBigDecimal(String.valueOf(oArr[1]));
        }
        return new QuestionEchartVo(xList, yList);
    }

    /**
     * ?????????????????????????????????????????????
     */
    @GetMapping("bar_data")
    @ResponseBody
    public QuestionEchartVo bar_data(String itemName, QuestionSearchVo questionSearch){
        List<Object[]> objects = questionService.findGroupData(itemName, questionSearch);
        String[] xList = new String[]{"????????????","?????????","??????","????????????","?????????","?????????"};
        BigDecimal[] yList = new BigDecimal[]{new BigDecimal(0),new BigDecimal(0),new BigDecimal(0),new BigDecimal(0),new BigDecimal(0),new BigDecimal(0)};
        for(int i=0;i<objects.size();i++){
            Object[] oArr = objects.get(i);
            String name = String.valueOf(oArr[0]);
            switch (name){
                case "6" : yList[0] = NumberUtil.toBigDecimal(String.valueOf(oArr[1])); break;
                case "5" : yList[1] = NumberUtil.toBigDecimal(String.valueOf(oArr[1])); break;
                case "4" : yList[2] = NumberUtil.toBigDecimal(String.valueOf(oArr[1])); break;
                case "3" : yList[3] = NumberUtil.toBigDecimal(String.valueOf(oArr[1])); break;
                case "2" : yList[4] = NumberUtil.toBigDecimal(String.valueOf(oArr[1])); break;
                case "1" : yList[5] = NumberUtil.toBigDecimal(String.valueOf(oArr[1])); break;
            }
        }
        return new QuestionEchartVo(xList, yList);
    }

    /**
     * excel??????
     */
    @GetMapping("export")
    @ResponseBody
    public void export(QuestionSearchVo questionSearch) throws IOException {
        List<QuestionExportVo> exportVoList = questionService.findExportData(questionSearch);
        List<QuestionExportVo> rows = CollUtil.newArrayList(exportVoList);

        ExcelUtils.export(response, "???????????????", rows, getQuestionExportNameValueVoList());
    }

    private List<NameValueVo> getQuestionExportNameValueVoList(){
        List<NameValueVo> nameValueVos = new ArrayList<>();
        nameValueVos.add(new NameValueVo("????????????", "hospitalName"));
        nameValueVos.add(new NameValueVo("????????????", "objectType"));
        nameValueVos.add(new NameValueVo("??????", "sex"));
        nameValueVos.add(new NameValueVo("?????????", "ageField"));
        nameValueVos.add(new NameValueVo("??????????????????", "qualityIndoor"));
        nameValueVos.add(new NameValueVo("??????????????????", "qualityOutdoor"));
        nameValueVos.add(new NameValueVo("??????????????????", "toiletHygiene"));
        nameValueVos.add(new NameValueVo("??????????????????", "cleanService"));
        nameValueVos.add(new NameValueVo("??????????????????", "dailySecurity"));
        nameValueVos.add(new NameValueVo("?????????????????????", "accidentalDisposal"));
        nameValueVos.add(new NameValueVo("??????????????????", "securityService"));
        nameValueVos.add(new NameValueVo("????????????", "dishPrice"));
        nameValueVos.add(new NameValueVo("????????????", "diningEnvironment"));
        nameValueVos.add(new NameValueVo("??????????????????", "foodService"));
        nameValueVos.add(new NameValueVo("??????????????????", "deliveryTimeliness"));
        nameValueVos.add(new NameValueVo("??????????????????", "foodNutrition"));
        nameValueVos.add(new NameValueVo("??????????????????", "diningAttitude"));
        nameValueVos.add(new NameValueVo("???????????????", "transportTimeliness"));
        nameValueVos.add(new NameValueVo("???????????????", "transportAccuracy"));
        nameValueVos.add(new NameValueVo("??????????????????", "transportService"));
        nameValueVos.add(new NameValueVo("???????????????", "repairTimeliness"));
        nameValueVos.add(new NameValueVo("????????????", "repairQuality"));
        nameValueVos.add(new NameValueVo("???????????????", "elevatorStatus"));
        nameValueVos.add(new NameValueVo("??????????????????", "operationService"));
        return nameValueVos;
    }

    // =========================== ??????????????? ?????? =================================

    /**
     * ??????????????????????????????????????????????????????????????????
     */
    @GetMapping("getQuestionCount")
    @ResponseBody
    public Map<String, Object> getQuestionCount(QuestionSearchVo questionSearchVo){
        long flagCount = 100l;
        List<Rule> rules = new ArrayList<>();
        rules.add(Rule.eq("state", BaseEnumManager.StateEnum.Enable));
        if(StrUtil.isNotBlank(questionSearchVo.getHospitalName())){
            rules.add(Rule.eq("hospitalName", questionSearchVo.getHospitalName()));
        }
        if(StrUtil.isNotBlank(questionSearchVo.getStartDate())){
            rules.add(Rule.ge("createTime", DateUtil.parseDate(questionSearchVo.getStartDate())));
        }
        if(StrUtil.isNotBlank(questionSearchVo.getEndDate())){
            rules.add(Rule.le("createTime", DateUtil.parseDate(questionSearchVo.getEndDate())));
        }

        //?????????
        long count_total = questionService.count(SpecificationFactory.where(rules));


        long count_HUANZHE;double percent_HUANZHE;long count_YIHURENYUAN;double percent_YIHURENYUAN;
        if(StrUtil.isBlank(questionSearchVo.getHospitalName())){
            //?????????????????????????????????????????????????????????????????????
            //?????????????????????????????????
            int hospCount = questionService.getHospCountEnable(questionSearchVo);
            if(hospCount > 0){
                flagCount = Convert.toLong(NumberUtil.mul(flagCount, hospCount));
            }
        }

        //???????????????
        List<Rule> rules_HUANZHE = new ArrayList<>(rules);
        rules_HUANZHE.add(Rule.in("objectType", new BaseEnumManager.ObjectTypeEnum[]{BaseEnumManager.ObjectTypeEnum.Patient, BaseEnumManager.ObjectTypeEnum.PatientFamily}));
        count_HUANZHE = questionService.count(SpecificationFactory.where(rules_HUANZHE));

        //??????????????????
        percent_HUANZHE = NumberUtil.mul(NumberUtil.div(count_HUANZHE, flagCount, 2), 100);
        if(percent_HUANZHE>100) percent_HUANZHE=100;

        //?????????????????????
        List<Rule> rules_YIHURENYUAN = new ArrayList<>(rules);
        rules_YIHURENYUAN.add(Rule.in("objectType", new BaseEnumManager.ObjectTypeEnum[]{BaseEnumManager.ObjectTypeEnum.Doctor, BaseEnumManager.ObjectTypeEnum.Nurse, BaseEnumManager.ObjectTypeEnum.Other}));
        count_YIHURENYUAN = questionService.count(SpecificationFactory.where(rules_YIHURENYUAN));

        //????????????????????????
        percent_YIHURENYUAN = NumberUtil.mul(NumberUtil.div(count_YIHURENYUAN, flagCount, 2), 100);
        if(percent_YIHURENYUAN>100) percent_YIHURENYUAN=100;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("count_total", count_total);
        data.put("count_HUANZHE", count_HUANZHE);
        data.put("percent_HUANZHE", percent_HUANZHE);
        data.put("count_YIHURENYUAN", count_YIHURENYUAN);
        data.put("percent_YIHURENYUAN", percent_YIHURENYUAN);
        return data;
    }

    /**
     * ????????????
     */
    @GetMapping("list")
    @ResponseBody
    public JqGridReturn list(QuestionSearchVo questionSearchVo){
        List<Map<String, Object>> dataRows = new ArrayList<>();
        List<QuestionCountVo> questionCountVoList = questionService.findQuestionCountList(questionSearchVo);
        questionCountVoList.forEach(count -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", count.getHospitalName());
            map.put("hospitalName", count.getHospitalName());
            map.put("objectType", count.getObjectType());
            map.put("count", count.getCount());
            map.put("maxCreateTime", count.getMaxCreateTime());
            dataRows.add(map);
        });
        return getJqGridReturn(dataRows, null);
    }

    /**
     * ?????????????????????????????????
     */
    @GetMapping("exportSummaryCountOfHospital")
    @ResponseBody
    public void exportSummaryCountOfHospital(QuestionSearchVo questionSearchVo, String endDate) throws IOException {
        if(StrUtil.isBlank(questionSearchVo.getHospitalName())) return;
		List<Rule> rulesCommon = new ArrayList<>();
        rulesCommon.add(Rule.eq("state", BaseEnumManager.StateEnum.Enable));
        rulesCommon.add(Rule.eq("hospitalName", questionSearchVo.getHospitalName()));
		if(StrUtil.isNotBlank(questionSearchVo.getStartDate())){
            rulesCommon.add(Rule.ge("createTime", DateUtil.parseDate(questionSearchVo.getStartDate())));
		}
		if(StrUtil.isNotBlank(questionSearchVo.getEndDate())){
            rulesCommon.add(Rule.le("createTime", DateUtil.parseDate(questionSearchVo.getEndDate())));
		}


		//?????????excel??????
        String path = "/file/export_temp.xlsx";
        InputStream inputStream = this.getClass().getResourceAsStream(path);
        File tempFile = FileUtil.writeFromStream(inputStream, "/tmp/excel.xlsx");


        //?????????????????????????????????????????????????????????????????????XY?????????????????????????????????????????????????????????????????????????????????
        Integer startY = 12;
        List<Integer> socreList = CollectionUtil.newArrayList(6,5,4,3,2,1);
        List<String> socreLocationXList = CollectionUtil.newArrayList("B","C","D","E","F","G");
        List<Dict> scoreTypeDictList = CollectionUtil.newArrayList(
                Dict.create().set("name", "qualityIndoor").set("desc", "??????????????????"),
                Dict.create().set("name", "qualityOutdoor").set("desc", "??????????????????"),
                Dict.create().set("name", "toiletHygiene").set("desc", "??????????????????"),
                Dict.create().set("name", "cleanService").set("desc", "??????????????????"),
                Dict.create().set("name", "dailySecurity").set("desc", "??????????????????"),
                Dict.create().set("name", "accidentalDisposal").set("desc", "?????????????????????"),
                Dict.create().set("name", "securityService").set("desc", "??????????????????"),
                Dict.create().set("name", "dishPrice").set("desc", "????????????"),
                Dict.create().set("name", "diningEnvironment").set("desc", "????????????"),
                Dict.create().set("name", "foodService").set("desc", "??????????????????"),
                Dict.create().set("name", "deliveryTimeliness").set("desc", "??????????????????"),
                Dict.create().set("name", "foodNutrition").set("desc", "??????????????????"),
                Dict.create().set("name", "transportTimeliness").set("desc", "???????????????"),
                Dict.create().set("name", "transportAccuracy").set("desc", "???????????????"),
                Dict.create().set("name", "transportService").set("desc", "??????????????????"),
                Dict.create().set("name", "repairTimeliness").set("desc", "???????????????"),
                Dict.create().set("name", "repairQuality").set("desc", "????????????"),
                Dict.create().set("name", "elevatorStatus").set("desc", "???????????????"),
                Dict.create().set("name", "operationService").set("desc", "??????????????????")
        );


        // ============================== ??????????????????????????? ==============================
        ExcelWriter excelWriter = ExcelUtil.getWriter(tempFile,"?????????");
        List<Rule> rulesCommon_huanzhe = new ArrayList<>(rulesCommon);
        rulesCommon_huanzhe.add(Rule.in("objectType", CollectionUtil.newArrayList(BaseEnumManager.ObjectTypeEnum.Patient, BaseEnumManager.ObjectTypeEnum.PatientFamily)));


        //??????????????????????????????????????????
        CellStyle cellStyleBlue = excelWriter.getOrCreateCellStyle("B12");
        //??????????????????????????????????????????
        CellStyle cellStyleWhite = excelWriter.getOrCreateCellStyle("B5");


        //??????????????????
        String hospName = "";
		try{
            hospName = BaseEnumManager.HospitalName.valueOf(questionSearchVo.getHospitalName()).value() + "???????????????????????????";
        }catch (Exception e){
            hospName = "???????????????????????????";
        }
        CellStyle cellStyleTitle = excelWriter.getOrCreateCellStyle("A1");
        excelWriter.writeCellValue("A1", hospName);
        excelWriter.setStyle(cellStyleTitle, "A1");


        //????????????
        String statisticsTime = "???";
        if(StrUtil.isNotBlank(questionSearchVo.getStartDate())) statisticsTime = questionSearchVo.getStartDate() + statisticsTime;
        if(StrUtil.isNotBlank(endDate)) statisticsTime = statisticsTime + endDate;
        excelWriter.writeCellValue("B2", statisticsTime);
        excelWriter.setStyle(cellStyleBlue, "B2");


        //??????
        long totalCount = questionService.count(SpecificationFactory.where(rulesCommon_huanzhe));
        excelWriter.writeCellValue("G2", Convert.toStr(totalCount));
        excelWriter.setStyle(cellStyleBlue, "G2");


        //??????????????????
        List<String> objectTypeList = CollectionUtil.newArrayList("Patient","PatientFamily");
        List<String> objectTypeLocationList = CollectionUtil.newArrayList("B6","C6");
        for(int i=0;i<objectTypeList.size();i++){
            List<Rule> rules = new ArrayList<>(rulesCommon_huanzhe);
            rules.add(Rule.eq("objectType", BaseEnumManager.ObjectTypeEnum.valueOf(objectTypeList.get(i))));
            long count = questionService.count(SpecificationFactory.where(rules));
            excelWriter.writeCellValue(objectTypeLocationList.get(i), Convert.toStr(count));
            excelWriter.setStyle(cellStyleBlue, objectTypeLocationList.get(i));
        }


        //??????
        List<String> sexList = CollectionUtil.newArrayList("male","female");
        List<String> sexLocationList = CollectionUtil.newArrayList("B8","C8");
        for(int i=0;i<sexList.size();i++){
            List<Rule> rules = new ArrayList<>(rulesCommon_huanzhe);
            rules.add(Rule.eq("sex", BaseEnumManager.SexEnum.valueOf(sexList.get(i))));
            long count = questionService.count(SpecificationFactory.where(rules));
            excelWriter.writeCellValue(sexLocationList.get(i), Convert.toStr(count));
            excelWriter.setStyle(cellStyleWhite, sexLocationList.get(i));
        }


        //?????????
        List<Integer> ageFieldList = CollectionUtil.newArrayList(1,2,4,6);
        List<String> ageFieldLocationList = CollectionUtil.newArrayList("B10","C10","D10","E10");
        for(int i=0;i<ageFieldList.size();i++){
            List<Rule> rules = new ArrayList<>(rulesCommon_huanzhe);
            rules.add(Rule.eq("ageField", ageFieldList.get(i)));
            long count = questionService.count(SpecificationFactory.where(rules));
            excelWriter.writeCellValue(ageFieldLocationList.get(i), Convert.toStr(count));
            excelWriter.setStyle(cellStyleBlue, ageFieldLocationList.get(i));
        }


        //???????????????????????????
        for(int i=0;i<scoreTypeDictList.size();i++){
            ++startY;
            CellStyle cellStyle = i%2>0 ? cellStyleBlue : cellStyleWhite;
            excelWriter.writeCellValue("A"+startY, scoreTypeDictList.get(i).getStr("desc"));
            excelWriter.setStyle(cellStyle, "A"+startY);
            for(int j=0;j<socreList.size();j++){
                List<Rule> rules = new ArrayList<>(rulesCommon_huanzhe);
                rules.add(Rule.eq(scoreTypeDictList.get(i).getStr("name"), socreList.get(j)));
                long count = questionService.count(SpecificationFactory.where(rules));
                excelWriter.writeCellValue(socreLocationXList.get(j)+startY, Convert.toStr(count));
                excelWriter.setStyle(cellStyle, socreLocationXList.get(j)+startY);
            }
        }

        // ============================== ??????????????????????????? ==============================
        excelWriter.setSheet("?????????");
        List<Rule> rulesCommon_yisheng = new ArrayList<>(rulesCommon);
        rulesCommon_yisheng.add(Rule.in("objectType", CollectionUtil.newArrayList(BaseEnumManager.ObjectTypeEnum.Doctor, BaseEnumManager.ObjectTypeEnum.Nurse, BaseEnumManager.ObjectTypeEnum.Other)));


        //??????????????????????????????????????????
        CellStyle cellStyleBlue_yisheng = excelWriter.getOrCreateCellStyle("B12");
        //??????????????????????????????????????????
        CellStyle cellStyleWhite_yisheng = excelWriter.getOrCreateCellStyle("B5");


        //??????????????????
        String hospName_yisheng = "";
        try{
            hospName_yisheng = BaseEnumManager.HospitalName.valueOf(questionSearchVo.getHospitalName()).value() + "???????????????????????????";
        }catch (Exception e){
            hospName_yisheng = "???????????????????????????";
        }
        CellStyle cellStyleTitle_yisheng = excelWriter.getOrCreateCellStyle("A1");
        excelWriter.writeCellValue("A1", hospName_yisheng);
        excelWriter.setStyle(cellStyleTitle_yisheng, "A1");


        //????????????
        if(StrUtil.isNotBlank(questionSearchVo.getStartDate())) statisticsTime = questionSearchVo.getStartDate() + statisticsTime;
        if(StrUtil.isNotBlank(endDate)) statisticsTime = statisticsTime + endDate;
        excelWriter.writeCellValue("B2", statisticsTime);
        excelWriter.setStyle(cellStyleBlue_yisheng, "B2");


        //??????
        long totalCount_yisheng = questionService.count(SpecificationFactory.where(rulesCommon_yisheng));
        excelWriter.writeCellValue("G2", Convert.toStr(totalCount_yisheng));
        excelWriter.setStyle(cellStyleBlue_yisheng, "G2");


        //??????????????????
        List<String> objectTypeList_yisheng = CollectionUtil.newArrayList("Doctor","Nurse","Other");
        List<String> objectTypeLocationList_yisheng = CollectionUtil.newArrayList("B6","C6","D6");
        for(int i=0;i<objectTypeLocationList_yisheng.size();i++){
            List<Rule> rules = new ArrayList<>(rulesCommon_yisheng);
            rules.add(Rule.eq("objectType", BaseEnumManager.ObjectTypeEnum.valueOf(objectTypeList_yisheng.get(i))));
            long count = questionService.count(SpecificationFactory.where(rules));
            excelWriter.writeCellValue(objectTypeLocationList_yisheng.get(i), Convert.toStr(count));
            excelWriter.setStyle(cellStyleBlue_yisheng, objectTypeLocationList_yisheng.get(i));
        }


        //??????
        List<String> sexList_yisheng = CollectionUtil.newArrayList("male","female");
        List<String> sexLocationList_yisheng = CollectionUtil.newArrayList("B8","C8");
        for(int i=0;i<sexList_yisheng.size();i++){
            List<Rule> rules = new ArrayList<>(rulesCommon_yisheng);
            rules.add(Rule.eq("sex", BaseEnumManager.SexEnum.valueOf(sexList_yisheng.get(i))));
            long count = questionService.count(SpecificationFactory.where(rules));
            excelWriter.writeCellValue(sexLocationList_yisheng.get(i), Convert.toStr(count));
            excelWriter.setStyle(cellStyleWhite_yisheng, sexLocationList_yisheng.get(i));
        }


        //?????????
        List<Integer> ageFieldList_yisheng = CollectionUtil.newArrayList(1,2,4,6);
        List<String> ageFieldLocationList_yisheng = CollectionUtil.newArrayList("B10","C10","D10","E10");
        for(int i=0;i<ageFieldList_yisheng.size();i++){
            List<Rule> rules = new ArrayList<>(rulesCommon_yisheng);
            rules.add(Rule.eq("ageField", ageFieldList_yisheng.get(i)));
            long count = questionService.count(SpecificationFactory.where(rules));
            excelWriter.writeCellValue(ageFieldLocationList_yisheng.get(i), Convert.toStr(count));
            excelWriter.setStyle(cellStyleBlue_yisheng, ageFieldLocationList_yisheng.get(i));
        }


        //???????????????????????????
        startY = 12;
        for(int i=0;i<scoreTypeDictList.size();i++){
            ++startY;
            CellStyle cellStyle = i%2>0 ? cellStyleBlue_yisheng : cellStyleWhite_yisheng;
            excelWriter.writeCellValue("A"+startY, scoreTypeDictList.get(i).getStr("desc"));
            excelWriter.setStyle(cellStyle, "A"+startY);
            for(int j=0;j<socreList.size();j++){
                List<Rule> rules = new ArrayList<>(rulesCommon_yisheng);
                rules.add(Rule.eq(scoreTypeDictList.get(i).getStr("name"), socreList.get(j)));
                long count = questionService.count(SpecificationFactory.where(rules));
                excelWriter.writeCellValue(socreLocationXList.get(j)+startY, Convert.toStr(count));
                excelWriter.setStyle(cellStyle, socreLocationXList.get(j)+startY);
            }
        }

        // =================================== ????????? ===========================================


        //?????????
        excelWriter.setSheet("?????????");
        List<QuestionExportVo> exportVoList = questionService.findExportData(questionSearchVo);
        List<QuestionExportVo> rows = CollUtil.newArrayList(exportVoList);
        List<NameValueVo> headNameKeyList = getQuestionExportNameValueVoList();
        for (int i = 0; i < headNameKeyList.size(); i++) {
            NameValueVo head = headNameKeyList.get(i);
            excelWriter.addHeaderAlias(head.getValue(), head.getName());
        }
        excelWriter.setColumnWidth(-1, 20);
        excelWriter.write(rows, true);


        //??????
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
		response.setHeader("Content-Disposition", "attachment;filename=" + new String((hospName + ".xlsx").getBytes(), "iso-8859-1"));
		ServletOutputStream out = response.getOutputStream();
		excelWriter.flush(out, true);
		excelWriter.close();
		IoUtil.close(out);
    }

}
