package com.duan.blogos.service.impl.blogger.profile;

import com.duan.blogos.dto.blogger.BloggerLinkDTO;
import com.duan.blogos.entity.blogger.BloggerLink;
import com.duan.blogos.result.ResultBean;
import com.duan.blogos.service.blogger.profile.LinkService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created on 2017/12/19.
 *
 * @author DuanJiaNing
 */
@Service("linkService")
public class LinkServiceImpl implements LinkService {
    @Override
    public ResultBean<List<BloggerLinkDTO>> listBloggerLink(int bloggerId, int offset, int rows) {
        return null;
    }

    @Override
    public int insertBloggerLink(int bloggerId, int iconId, String title, String url, String desc, int priority) {
        return 0;
    }

    @Override
    public BloggerLink deleteBloggerLink(int linkId) {
        return null;
    }

    @Override
    public boolean updateBloggerLink(int linkId, int newBloggerId, int newIconId, String newTitle, String newUrl, String newDesc, int newPriority) {
        return false;
    }
}