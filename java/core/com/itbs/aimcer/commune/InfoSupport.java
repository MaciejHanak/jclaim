package com.itbs.aimcer.commune;

import com.itbs.aimcer.bean.Nameable;

import java.util.List;

/**
 * @author Alex Rass
 * @since Nov 14, 2008 5:12:50 PM
 */
public interface InfoSupport extends Connection{
    List<String> getUserInfo(Nameable user);
    List<String> getUserInfoColumns();    
}
