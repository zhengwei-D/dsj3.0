package com.hanqian.kepler.web.controller.sys;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.hanqian.kepler.common.bean.jqgrid.JqGridContent;
import com.hanqian.kepler.common.bean.jqgrid.JqGridFilter;
import com.hanqian.kepler.common.bean.jqgrid.JqGridPager;
import com.hanqian.kepler.common.bean.jqgrid.JqGridReturn;
import com.hanqian.kepler.common.bean.result.AjaxResult;
import com.hanqian.kepler.common.enums.BaseEnumManager;
import com.hanqian.kepler.common.jpa.specification.Rule;
import com.hanqian.kepler.core.entity.primary.sys.Group;
import com.hanqian.kepler.core.service.sys.GroupService;
import com.hanqian.kepler.flow.entity.User;
import com.hanqian.kepler.security.annotation.CurrentUser;
import com.hanqian.kepler.web.annotation.RequestJsonParam;
import com.hanqian.kepler.web.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/main/group")
public class GroupController extends BaseController {

    @Autowired
    private GroupService groupService;

    @GetMapping("list")
    @ResponseBody
    public JqGridReturn list(@CurrentUser User user, JqGridPager pager, @RequestJsonParam("filters") JqGridFilter filters){
        Pageable pageable = getJqGridPageable(pager);
        List<Rule> rules = getJqGridSearch(filters);
        JqGridContent<Group> groupJqGridContent = groupService.getJqGridContent(rules, pageable);

        List<Map<String, Object>> dataRows = new ArrayList<>();
        groupJqGridContent.getList().forEach(group->{
            Map<String, Object> map = new HashMap<>();
            map.put("id", group.getId());
            map.put("name", group.getName());
            map.put("description", group.getDescription());
            map.put("createTime", group.getCreateTime()!=null ? DateUtil.formatDateTime(group.getCreateTime()) : "");
            map.put("userIds", group.getUserIds());
            map.put("count", StrUtil.split(group.getUserIds(), ",").length);
            dataRows.add(map);
        });
        return getJqGridReturn(dataRows, groupJqGridContent.getPage());
    }

    /**
     * ?????? ?????????
     */
    @GetMapping("listMember")
    @ResponseBody
    public JqGridReturn listMember(String keyId){
        List<Map<String, Object>> dataRows = new ArrayList<>();
        Group group = groupService.get(keyId);
        if(group!=null){
            groupService.findUsersByGroup(group).forEach(user -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", user.getId());
                map.put("name", user.getName());
                dataRows.add(map);
            });
        }
        return getJqGridReturn(dataRows, null);
    }

    /**
     * input
     */
    @GetMapping("input")
    public String input(String keyId, Model model){
        Group group = groupService.get(keyId);
        model.addAttribute("group", group);
        return "/main/sys/group_input";
    }

    /**
     * ??????????????????
     */
    @PostMapping(value = "validate/name")
    @ResponseBody
    public String validate_account(String name, String keyId) {
        Group group = groupService.getGroupByName(name);
        return String.valueOf(group==null || (StrUtil.isNotBlank(keyId) && StrUtil.equals(keyId, group.getId())));
    }

    /**
     * ????????????
     */
    @PostMapping("save")
    @ResponseBody
    public AjaxResult save(String keyId, String name, String description){
        Group group = groupService.get(keyId);
        if(group == null){
            group = new Group();
        }
        group.setName(name);
        group.setDescription(description);
        groupService.save(group);
        return AjaxResult.success();
    }

    /**
     * ?????????????????????
     */
    @PostMapping("setMembers")
    @ResponseBody
    public AjaxResult setMembers(String keyId, String userIds){
        Group group = groupService.get(keyId);
        if(group == null) return AjaxResult.error("?????????????????????");

        group.setUserIds(userIds);
        groupService.save(group);
        return AjaxResult.success();
    }

    /**
     * ??????
     */
    @PostMapping("delete")
    @ResponseBody
    public AjaxResult delete(String keyId){
        Group group = groupService.get(keyId);
        if(group == null) return AjaxResult.error("?????????????????????");

        group.setState(BaseEnumManager.StateEnum.Delete);
        groupService.save(group);
        return AjaxResult.success();
    }

}
