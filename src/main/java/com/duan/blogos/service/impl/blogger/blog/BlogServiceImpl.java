package com.duan.blogos.service.impl.blogger.blog;

import com.duan.blogos.dao.blog.BlogCategoryDao;
import com.duan.blogos.dao.blog.BlogStatisticsDao;
import com.duan.blogos.dao.blogger.BloggerPictureDao;
import com.duan.blogos.dto.blogger.BlogListItemDTO;
import com.duan.blogos.dto.blogger.BlogStatisticsDTO;
import com.duan.blogos.entity.blog.Blog;
import com.duan.blogos.entity.blog.BlogCategory;
import com.duan.blogos.entity.blog.BlogStatistics;
import com.duan.blogos.enums.BlogStatusEnum;
import com.duan.blogos.exception.BaseRuntimeException;
import com.duan.blogos.exception.LuceneException;
import com.duan.blogos.manager.BlogSortRule;
import com.duan.blogos.manager.DataFillingManager;
import com.duan.blogos.manager.WebsitePropertiesManager;
import com.duan.blogos.result.ResultBean;
import com.duan.blogos.service.BlogFilterAbstract;
import com.duan.blogos.service.blogger.blog.BlogService;
import com.duan.blogos.util.CollectionUtils;
import com.duan.blogos.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2017/12/19.
 * 博主检索博文
 *
 * @author DuanJiaNing
 */
@Service
public class BlogServiceImpl extends BlogFilterAbstract<ResultBean<List<BlogListItemDTO>>> implements BlogService {

    @Autowired
    private BlogStatisticsDao statisticsDao;

    @Autowired
    private WebsitePropertiesManager websitePropertiesManager;

    @Autowired
    private DataFillingManager dataFillingManager;

    @Autowired
    private BlogCategoryDao categoryDao;

    @Autowired
    private BloggerPictureDao pictureDao;

    @Override
    public int insertBlog(int bloggerId, int[] categories, int[] labels,
                          BlogStatusEnum status, String title, String content,
                          String summary, String[] keyWords) {

        // 1 插入数据到bolg表
        String ch = dbPropertiesManager.getStringFiledSplitCharacterForNumber();
        String chs = dbPropertiesManager.getStringFiledSplitCharacterForString();
        Blog blog = new Blog();
        blog.setBloggerId(bloggerId);
        blog.setCategoryIds(StringUtils.intArrayToString(categories, ch));
        blog.setLabelIds(StringUtils.intArrayToString(labels, ch));
        blog.setState(status.getCode());
        blog.setTitle(title);
        blog.setContent(content);
        blog.setSummary(summary);
        blog.setKeyWords(StringUtils.arrayToString(keyWords, chs));
        blog.setWordCount(content.length());

        int effect = blogDao.insert(blog);
        if (effect <= 0) return -1;

        int blogId = blog.getId();

        // 2 插入数据到blog_statistics表（生成博文信息记录）
        BlogStatistics statistics = new BlogStatistics();
        statistics.setBlogId(blogId);
        effect = statisticsDao.insert(statistics);
        if (effect <= 0) throw new BaseRuntimeException("insert blog statistic fail when insert blog");

        // 3 解析本地图片引用并使自增
        int[] imids = parseContentForImageIds(content, bloggerId);
        if (!CollectionUtils.isEmpty(imids))
            Arrays.stream(imids).forEach(pictureDao::updateUseCountPlus);

        // 4 lucene创建索引
        try {
            luceneIndexManager.add(blog);
        } catch (IOException e) {
            e.printStackTrace();
            throw new LuceneException(e);
        }

        return blogId;
    }

    // 解析博文中引用的相册图片
    private int[] parseContentForImageIds(String content, int bloggerId) {
        String regex = "http://" + websitePropertiesManager.getAddr() + "/image/" + bloggerId + "/(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);

        List<String> res = new ArrayList<>();
        while (matcher.find()) {
            String str = matcher.group();
            int index = str.lastIndexOf("/");
            res.add(str.substring(index + 1));
        }

        return res.stream()
                .mapToInt(Integer::valueOf)
                .distinct()
                .toArray();
    }

    @Override
    public boolean updateBlog(int blogId, int newBloggerId, int[] newCategories, int[] newLabels, BlogStatusEnum newStatus, String newTitle, String newContent, String newSummary, String[] newKeyWords) {
        return false;
    }

    @Override
    public boolean updateBlogCategory(int blogId, int[] newCategories) {
        return false;
    }

    @Override
    public boolean updateBlogLabel(int blogId, int[] newCategories) {
        return false;
    }

    @Override
    public boolean updateBlogState(int blogId, BlogStatusEnum newStatus) {
        return false;
    }

    @Override
    public boolean updateBlogTitle(int blogId, String newTitle) {
        return false;
    }

    @Override
    public boolean updateBlogSummary(int blogId, String newSummary) {
        return false;
    }

    @Override
    public boolean updateBlogKeyWords(int blogId, String[] newKeyWords) {
        return false;
    }

    @Override
    public Blog deleteBlog(int blogId) {
        return null;
    }

    @Override
    public boolean deleteBlogPatch(int[] blogIds) {
        return false;
    }

    @Override
    public ResultBean<BlogStatisticsDTO> getBlogStatistics(int blogId) {
        return null;
    }

    @Override
    public ResultBean<List<BlogListItemDTO>> listFilterByStatus(int bloggerId, BlogStatusEnum status, int offset,
                                                                int rows, BlogSortRule sortRule) {
        return null;
    }

    @Override
    public boolean getBlogForCheckExist(int blogId) {
        return !(blogDao.getBlogIdById(blogId) == null);
    }

    @Override
    public ResultBean<Blog> getBlog(int bloggerId, int blogId) {

        Blog blog = blogDao.getBlogById(blogId);

        String ch = dbPropertiesManager.getStringFiledSplitCharacterForNumber();
        String chs = dbPropertiesManager.getStringFiledSplitCharacterForString();
        String whs = websitePropertiesManager.getUrlConditionSplitCharacter();
        if (blog != null && blog.getBloggerId().equals(bloggerId)) {

            blog.setCategoryIds(blog.getCategoryIds().replace(ch, whs));
            blog.setLabelIds(blog.getLabelIds().replace(ch, whs));
            blog.setKeyWords(blog.getKeyWords().replace(chs, whs));
            return new ResultBean<>(blog);

        }

        return null;
    }

    @Override
    protected ResultBean<List<BlogListItemDTO>> constructResult(Map<Integer, Blog> blogHashMap, List<BlogStatistics> statistics,
                                                                Map<Integer, int[]> blogIdMapCategoryIds) {
        // 重组结果
        List<BlogListItemDTO> result = new ArrayList<>();
        for (BlogStatistics s : statistics) {
            Integer blogId = s.getBlogId();
            int[] ids = blogIdMapCategoryIds.get(blogId);
            List<BlogCategory> categories = CollectionUtils.isEmpty(ids) ? null : categoryDao.listCategoryById(ids);
            Blog blog = blogHashMap.get(blogId);
            BlogListItemDTO dto = dataFillingManager.bloggerBlogListItemToDTO(blog, s, categories);
            result.add(dto);
        }

        return new ResultBean<>(result);
    }
}
