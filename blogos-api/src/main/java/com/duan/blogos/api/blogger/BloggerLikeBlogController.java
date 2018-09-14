package com.duan.blogos.api.blogger;

import com.duan.blogos.service.common.BlogSortRule;
import com.duan.blogos.service.common.Order;
import com.duan.blogos.service.common.Rule;
import com.duan.blogos.service.dto.blogger.FavouriteBlogListItemDTO;
import com.duan.blogos.service.exception.CodeMessage;
import com.duan.blogos.service.exception.ResultUtil;
import com.duan.blogos.service.restful.ResultBean;
import com.duan.blogos.service.service.blogger.BloggerLikeBlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created on 2018/1/9.
 * 博主收藏博文
 * <p>
 * 1 收藏博文清单
 * 2 取消博文收藏
 * 3 修改博文收藏
 *
 * @author DuanJiaNing
 */
@RestController
@RequestMapping("/blogger/{bloggerId}/like")
public class BloggerLikeBlogController extends BaseBloggerController {

    @Autowired
    private BloggerLikeBlogService likeBlogService;

    /**
     * 收藏博文清单
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResultBean<List<FavouriteBlogListItemDTO>> list(HttpServletRequest request,
                                                           @PathVariable("bloggerId") Integer bloggerId,
                                                           @RequestParam(value = "offset", required = false) Integer offset,
                                                           @RequestParam(value = "rows", required = false) Integer rows,
                                                           @RequestParam(value = "sort", required = false) String sort,
                                                           @RequestParam(value = "order", required = false) String order) {
        final RequestContext context = new RequestContext(request);
        handleAccountCheck(request, bloggerId);

        //检查数据合法性
        String sor = sort == null ? Rule.VIEW_COUNT.name() : sort.toUpperCase();
        String ord = order == null ? Order.DESC.name() : order.toUpperCase();
        if (!Rule.contains(sor))
            throw ResultUtil.failException(CodeMessage.BLOG_BLOG_SORT_RULE_UNDEFINED);

        if (!Order.contains(ord))
            throw ResultUtil.failException(CodeMessage.BLOG_BLOG_SORT_ORDER_UNDEFINED);

        int os = offset == null || offset < 0 ? 0 : offset;
        int rs = rows == null || rows < 0 ? bloggerProperties.getRequestBloggerCollectCount() : rows;

        // 查询数据
        ResultBean<List<FavouriteBlogListItemDTO>> result = likeBlogService.listLikeBlog(bloggerId, os, rs,
                BlogSortRule.valueOf(sor, ord));
        if (result == null) handlerEmptyResult();

        return result;
    }


    /**
     * 统计收藏收藏量
     */
    @RequestMapping("/count")
    public ResultBean count(HttpServletRequest request,
                            @PathVariable("bloggerId") Integer bloggerId) {

        handleAccountCheck(request, bloggerId);

        return new ResultBean<>(likeBlogService.countByBloggerId(bloggerId));
    }
}