package com.entdiy.dev.guide.web;

import com.entdiy.auth.entity.Department;
import com.entdiy.auth.entity.Privilege;
import com.entdiy.auth.service.DepartmentService;
import com.entdiy.auth.service.PrivilegeService;
import com.entdiy.auth.service.UserService;
import com.entdiy.core.annotation.MetaData;
import com.entdiy.core.entity.BaseNativeEntity;
import com.entdiy.core.pagination.GroupPropertyFilter;
import com.entdiy.core.pagination.PropertyFilter;
import com.entdiy.core.service.BaseService;
import com.entdiy.core.service.GlobalConfigService;
import com.entdiy.core.util.DateUtils;
import com.entdiy.core.web.BaseController;
import com.entdiy.core.web.filter.WebAppContextInitFilter;
import com.entdiy.core.web.view.OperationResult;
import com.entdiy.security.AuthUserDetails;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.options.MutableDataSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.util.ClassUtils;
import org.hibernate.envers.Audited;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.math.BigDecimal;
import java.net.JarURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;

@Controller
@RequestMapping(value = "/dev/docs")
public class DocumentController extends BaseController<DocumentController.MockEntity, Long> {

    private final static Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private PrivilegeService privilegeService;

    @RequestMapping(value = "/markdown/{name}", method = RequestMethod.GET)
    public String markdown(HttpServletRequest request, @PathVariable("name") String name, Model model) throws Exception {
        List<String> fileNames = Lists.newArrayList();
        String text;
        String dir = "/dev/docs/markdown";

        String mdDirPath = WebAppContextInitFilter.getInitedWebContextRealPath() + dir;
        File mdDir = new File(mdDirPath);
        if (mdDir.exists()) { //直接web目录方式部署资源
            String mdFilePath = mdDirPath + "/" + name + ".md";
            text = FileUtils.readFileToString(new File(mdFilePath), "UTF-8");

            String[] files = mdDir.list();
            for (int i = 0; i < files.length; i++) {
                fileNames.add(StringUtils.substringBeforeLast(files[i], ".md"));
            }
        } else {//如果目录不存在，兼容Servlet3协议从classpath或jar的META-INFO/resources读取
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            dir = "META-INF/resources" + dir;
            text = IOUtils.toString(loader.getResourceAsStream(dir + "/" + name + ".md"), "UTF-8");

            URL url = loader.getResource(dir);
            if ("file".equalsIgnoreCase(url.getProtocol())) {//兼容处理JRebel直接以classpath形式加载
                File mdFilesDir = new File(url.toURI());
                String[] files = mdFilesDir.list();
                for (int i = 0; i < files.length; i++) {
                    fileNames.add(StringUtils.substringBeforeLast(files[i], ".md"));
                }
            } else { //基于jar文件提取特定目录下文件列表
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                Enumeration<JarEntry> jarEntries = connection.getJarFile().entries();
                while (jarEntries.hasMoreElements()) {
                    JarEntry jarEntry = jarEntries.nextElement();
                    if (!jarEntry.isDirectory()) {
                        String path = jarEntry.getName();
                        if (path.startsWith(dir) && path.endsWith(".md")) {
                            fileNames.add(StringUtils.substringBeforeLast(StringUtils.substringAfterLast(path, "/"), ".md"));
                        }
                    }
                }
            }
        }

        model.addAttribute("files", fileNames);

        MutableDataSet options = new MutableDataSet();
        // uncomment to set optional extensions
        options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create()));
        // uncomment to convert soft-breaks to hard breaks
        //options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
        // You can re-use parser and renderer instances
        Node document = parser.parse(text);
        String html = renderer.render(document);
        model.addAttribute("mdHtml", html);

        return "dev/layouts/markdown";
    }

    @Override
    protected BaseService<MockEntity, Long> getEntityService() {
        return null;
    }

    @ModelAttribute
    public void prepareModel(HttpServletRequest request, Model model, @RequestParam(value = "id", required = false) Long id) {
        //只允许在开发模式下访问
        Assert.isTrue(GlobalConfigService.isDevMode());
        super.initPrepareModel(request, model, id);
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, "publishTime", new CustomDateEditor(new SimpleDateFormat(DateUtils.SHORT_TIME_FORMAT), true));
        super.initBinder(binder);
    }

    @RequiresRoles(AuthUserDetails.ROLE_MGMT_USER)
    @RequestMapping(value = "/ui-feature/items", method = RequestMethod.GET)
    public String uiFeatureItems(Model model) {
        Map<String, String> clazzMapping = Maps.newHashMap();
        //搜索所有entity对象，并自动进行自增初始化值设置
        ClassPathScanningCandidateComponentProvider scan = new ClassPathScanningCandidateComponentProvider(false);
        scan.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        Set<BeanDefinition> beanDefinitions = scan.findCandidateComponents("**.entity.**");
        for (BeanDefinition beanDefinition : beanDefinitions) {
            Class<?> entityClass = ClassUtils.forName(beanDefinition.getBeanClassName());
            Audited audited = entityClass.getAnnotation(Audited.class);
            if (audited != null) {
                MetaData metaData = entityClass.getAnnotation(MetaData.class);
                if (metaData != null) {
                    clazzMapping.put(entityClass.getName(), metaData.value());
                } else {
                    clazzMapping.put(entityClass.getName(), entityClass.getName());
                }
            }
        }
        model.addAttribute("clazzMapping", clazzMapping);

        //典型的select下拉option数据项
        Map<Long, String> multiSelectOptions = Maps.newLinkedHashMap();
        multiSelectOptions.put(1L, "选项AAA");
        multiSelectOptions.put(2L, "中文BBB");
        multiSelectOptions.put(3L, "选项CCC");
        multiSelectOptions.put(4L, "元素DDD");
        model.addAttribute("multiSelectOptions", multiSelectOptions);

        //Tag类型的select下拉option数据项
        String[] multiSelectTags = new String[]{"标签AAA", "关键字BB", "选项CCC", "分类DDD"};
        model.addAttribute("multiSelectTags", multiSelectTags);

        MockEntity entity = new MockEntity();
        entity.setSelectedId(2L);
        entity.setSelectedIds(new Long[]{2L, 3L});
        entity.setTextContent("分类DDD");
        entity.setSplitText("标签AAA,选项CCC");
        Department department = new Department();
        department.setId(100L);
        department.setCode("MOCK100");
        department.setName("模拟部门100");
        entity.setDepartment(department);
        model.addAttribute("entity", entity);

        //构造用于remote类型select元素的初始化显示option数据项
        Map<Long, String> initSelectOption = Maps.newLinkedHashMap();
        initSelectOption.put(entity.getId(), entity.getTextContent());
        model.addAttribute("initSelectOption", initSelectOption);

        //上下文完整路径
        model.addAttribute("webContextFullUrl", WebAppContextInitFilter.getInitedWebContextFullUrl());

        return "dev/docs/ui-feature-items";
    }

    @RequestMapping(value = "/mock/show-form-data", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult showFormData(HttpServletRequest request) {
        Enumeration<String> names = request.getParameterNames();
        StringBuilder sb = new StringBuilder("服务器接收到的表单数据列表：");
        sb.append("<ol>");
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String[] values = request.getParameterValues(name);
            sb.append("<li><b>" + name + ":</b>" + StringUtils.join(values, ",") + "</li>");
        }
        sb.append("</ol>");
        return OperationResult.buildSuccessResult(sb.toString());
    }

    @RequestMapping(value = "/mock/tree-datas", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> mockTreeDatas(Model model) {
        List<Department> items = departmentService.findRoots();
        List<Map<String, Object>> treeDatas = Lists.newArrayList();

        for (Department item : items) {
            loopTreeData(treeDatas, item);
        }
        return treeDatas;
    }

    private void loopTreeData(List<Map<String, Object>> treeDatas, Department item) {
        Map<String, Object> row = Maps.newHashMap();
        treeDatas.add(row);
        row.put("id", item.getId());
        row.put("name", item.getDisplay());
        row.put("level", item.getLevel());
        row.put("open", false);
        List<Department> children = departmentService.findChildren(item);
        if (!CollectionUtils.isEmpty(children)) {
            List<Map<String, Object>> childrenList = Lists.newArrayList();
            row.put("children", childrenList);
            for (Department child : children) {
                loopTreeData(childrenList, child);
            }
        }
    }

    @RequestMapping(value = "/docs/mock/tags", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> tagsData(Model model, @RequestParam("q") String q) {
        List<Map<String, Object>> items = Lists.newArrayList();
        for (int i = 0, length = new Double((5 + Math.random() * 10)).intValue(); i < length; i++) {
            Map<String, Object> item = Maps.newHashMap();
            String txt = q + "模拟选项" + i;
            item.put("id", txt);
            item.put("text", txt);
            items.add(item);
        }
        return items;
    }

    @RequestMapping(value = "/ui-feature/dropdownselect", method = RequestMethod.GET)
    public String uiFeatureDropdownselect(Model model) {
        return "docs/ui-feature-dropdownselect";
    }

    public Map<Long, Object> mockRemoteSelectOptions(Model model, @RequestParam("code") String code) {
        Map<Long, Object> data = Maps.newLinkedHashMap();
        for (long i = 0; i < 10; i++) {
            data.put(i, code + "选项" + i);
        }
        return data;
    }

    @RequestMapping(value = "/mock/dynamic-table", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult saveDynamicTable(@ModelAttribute("entity") MockEntity entity, Model model) {
        logger.debug("MockEntity: {}", entity);

        //处理关联对象删除
        List<MockItemEntity> items = entity.getMockItemEntites();
        if (CollectionUtils.isNotEmpty(items)) {
            List<MockItemEntity> toRemoves = Lists.newArrayList();
            for (MockItemEntity item : items) {
                logger.debug("MockItemEntity: {}", item);
                if (item.isMarkedRemove()) {
                    toRemoves.add(item);
                }
            }
            items.removeAll(toRemoves);
        }

        return OperationResult.buildSuccessResult("数据处理成功");
    }

    @RequestMapping(value = "/mock/infinite-scroll", method = RequestMethod.GET)
    public Object repayLogData(HttpServletRequest request, Model model) {
        GroupPropertyFilter groupPropertyFilter = GroupPropertyFilter.buildFromHttpRequest(Privilege.class, request);
        Pageable pageable = PropertyFilter.buildPageableFromHttpRequest(request);
        model.addAttribute("pageData", privilegeService.findByPage(groupPropertyFilter, pageable));
        return "dev/docs/infinite-scroll-items";
    }

    @RequiresRoles(AuthUserDetails.ROLE_MGMT_USER)
    @RequestMapping(value = "/mock/btn-post", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult btnPost(Model model) {
        return OperationResult.buildSuccessResult("模拟POST数据处理成功");
    }

    @MetaData("模拟表单校验Confirm")
    @RequestMapping(value = "/mock/validation-confirm", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult validationConfirm(HttpServletRequest request, Model model, @RequestParam("quantity") Integer quantity) {
        //先进行常规的must数据校验

        //检测本次提交表单没有用户已confirm确认标识，则进行相关预警校验检查
        if (postNotConfirmedByUser(request)) {
            if (quantity > 100) {
                return OperationResult.buildConfirmResult("库存余量不足");
            }
        }

        return OperationResult.buildSuccessResult("模拟POST数据处理成功");
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @Access(AccessType.FIELD)
    public static class MockEntity extends BaseNativeEntity {
        private static final long serialVersionUID = -9187040717483523936L;

        private Department department;

        private String filePath;

        private Long selectedId;

        private Long[] selectedIds;

        @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_FORMAT)
        private Date saleDate = DateUtils.currentDate();

        @DateTimeFormat(pattern = DateUtils.SHORT_TIME_FORMAT)
        private Date publishTime = DateUtils.currentDate();

        private Date searchDate;

        private String textContent;

        private String htmlContent;

        private String splitText;

        private String[] emptyTexts;

        private String[] emptyTexts2;

        private String[] emptyTexts3;

        private String[] emptyTexts4;

        @MetaData(value = "图片路径数组", comments = "用于UI表单数据收集，实际可根据设计转换为另外的逗号分隔字符串存储属性")
        private String[] imagePaths;

        private BigDecimal quantity;

        private Boolean expired;

        private List<MockItemEntity> mockItemEntites;

        public String[] getSplitTexts() {
            return StringUtils.split(splitText, ",");
        }

        public void setSplitTexts(String[] splitTexts) {
            this.splitText = StringUtils.join(splitTexts, ",");
        }

        @Override
        public String toString() {
            return "MockEntity [department=" + department + ", filePath=" + filePath + ", selectedId=" + selectedId + ", selectedIds="
                    + Arrays.toString(selectedIds) + ", saleDate=" + saleDate + ", publishTime=" + publishTime + ", searchDate=" + searchDate
                    + ", textContent=" + textContent + ", htmlContent=" + htmlContent + ", splitText=" + splitText + ", imagePaths="
                    + Arrays.toString(imagePaths) + ", quantity=" + quantity + "]";
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @Access(AccessType.FIELD)
    public static class MockItemEntity extends BaseNativeEntity {

        private static final long serialVersionUID = -2048009679981043819L;

        private MockEntity mockEntity;

        private Department department;

        @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_FORMAT)
        private Date saleDate = DateUtils.currentDate();

        private String textContent;

        private String imagePath;

        @Override
        public String toString() {
            return "MockItemEntity [department=" + department + ", saleDate=" + saleDate + ", textContent=" + textContent + ", imagePath="
                    + imagePath + "]";
        }
    }

}
