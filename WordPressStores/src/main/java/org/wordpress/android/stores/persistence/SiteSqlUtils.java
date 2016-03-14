package org.wordpress.android.stores.persistence;

import android.content.ContentValues;

import com.wellsql.generated.SiteModelTable;
import com.yarolegovich.wellsql.WellSql;
import com.yarolegovich.wellsql.mapper.InsertMapper;

import org.wordpress.android.stores.model.SiteModel;

import java.util.List;

public class SiteSqlUtils {
    public static List<SiteModel> getAllSitesWith(String field, Object value) {
        return WellSql.select(SiteModel.class)
                .where().equals(field, value).endWhere()
                .getAsModel();
    }

    public static int getNumberOfSitesWith(String field, Object value) {
        return WellSql.select(SiteModel.class)
                .where().equals(field, value).endWhere()
                .getAsCursor().getCount();
    }

    public static void insertOrUpdateSite(SiteModel site) {
        if (site == null) {
            return;
        }
        List<SiteModel> siteResult = WellSql.select(SiteModel.class)
                .where().beginGroup()
                .equals(SiteModelTable.SITE_ID, site.getSiteId())
                .equals(SiteModelTable.URL, site.getUrl())
                .endGroup().endWhere().getAsModel();
        if (siteResult.isEmpty()) {
            // insert
            WellSql.insert(site).asSingleTransaction(true).execute();
        } else {
            // update
            int oldId = siteResult.get(0).getId();
            WellSql.update(SiteModel.class).whereId(oldId)
                    .put(site, new UpdateAllExceptId<SiteModel>()).execute();
        }
    }

    public static void deleteSite(SiteModel site) {
        if (site == null) {
            return;
        }
         WellSql.delete(SiteModel.class)
                 .where().equals(SiteModelTable.ID, site.getId()).endWhere()
                 .execute();
    }

    public static void setSiteVisibility(SiteModel site, boolean visible) {
        if (site == null) {
            return;
        }
        WellSql.update(SiteModel.class)
                .whereId(site.getId())
                .where().equals(SiteModelTable.IS_WPCOM, 1).endWhere()
                .put(visible, new InsertMapper<Boolean>() {
                    @Override
                    public ContentValues toCv(Boolean item) {
                        ContentValues cv = new ContentValues();
                        cv.put(SiteModelTable.IS_VISIBLE, item);
                        return cv;
                    }
                }).execute();
    }

    public static List<SiteModel> getAllRestApiSites() {
        return WellSql.select(SiteModel.class)
                .where().beginGroup()
                .equals(SiteModelTable.IS_WPCOM, 1)
                .or()
                .beginGroup()
                .equals(SiteModelTable.IS_JETPACK, 1)
                .equals(SiteModelTable.DOT_ORG_SITE_ID, 0)
                .endGroup()
                .endGroup().endWhere()
                .getAsModel();
    }
}
