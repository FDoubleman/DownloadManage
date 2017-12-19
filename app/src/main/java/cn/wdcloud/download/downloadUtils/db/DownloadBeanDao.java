package cn.wdcloud.download.downloadUtils.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * DAO for table "DOWNLOAD_BEAN".
*/
public class DownloadBeanDao extends AbstractDao<DownloadBean, Long> {

    public static final String TABLENAME = "DOWNLOAD_BEAN";

    /**
     * Properties of entity DownloadBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Url = new Property(1, String.class, "url", false, "URL");
        public final static Property TotalSize = new Property(2, Long.class, "totalSize", false, "TOTAL_SIZE");
        public final static Property CurrentSize = new Property(3, Long.class, "currentSize", false, "CURRENT_SIZE");
        public final static Property Progress = new Property(4, int.class, "progress", false, "PROGRESS");
        public final static Property Status = new Property(5, int.class, "status", false, "STATUS");
        public final static Property FilePath = new Property(6, String.class, "filePath", false, "FILE_PATH");
        public final static Property FileName = new Property(7, String.class, "fileName", false, "FILE_NAME");
        public final static Property StartTime = new Property(8, String.class, "startTime", false, "START_TIME");
    }


    public DownloadBeanDao(DaoConfig config) {
        super(config);
    }
    
    public DownloadBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DOWNLOAD_BEAN\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"URL\" TEXT," + // 1: url
                "\"TOTAL_SIZE\" INTEGER," + // 2: totalSize
                "\"CURRENT_SIZE\" INTEGER," + // 3: currentSize
                "\"PROGRESS\" INTEGER NOT NULL ," + // 4: progress
                "\"STATUS\" INTEGER NOT NULL ," + // 5: status
                "\"FILE_PATH\" TEXT," + // 6: filePath
                "\"FILE_NAME\" TEXT," + // 7: fileName
                "\"START_TIME\" TEXT);"); // 8: startTime
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DOWNLOAD_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DownloadBean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(2, url);
        }
 
        Long totalSize = entity.getTotalSize();
        if (totalSize != null) {
            stmt.bindLong(3, totalSize);
        }
 
        Long currentSize = entity.getCurrentSize();
        if (currentSize != null) {
            stmt.bindLong(4, currentSize);
        }
        stmt.bindLong(5, entity.getProgress());
        stmt.bindLong(6, entity.getStatus());
 
        String filePath = entity.getFilePath();
        if (filePath != null) {
            stmt.bindString(7, filePath);
        }
 
        String fileName = entity.getFileName();
        if (fileName != null) {
            stmt.bindString(8, fileName);
        }
 
        String startTime = entity.getStartTime();
        if (startTime != null) {
            stmt.bindString(9, startTime);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DownloadBean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(2, url);
        }
 
        Long totalSize = entity.getTotalSize();
        if (totalSize != null) {
            stmt.bindLong(3, totalSize);
        }
 
        Long currentSize = entity.getCurrentSize();
        if (currentSize != null) {
            stmt.bindLong(4, currentSize);
        }
        stmt.bindLong(5, entity.getProgress());
        stmt.bindLong(6, entity.getStatus());
 
        String filePath = entity.getFilePath();
        if (filePath != null) {
            stmt.bindString(7, filePath);
        }
 
        String fileName = entity.getFileName();
        if (fileName != null) {
            stmt.bindString(8, fileName);
        }
 
        String startTime = entity.getStartTime();
        if (startTime != null) {
            stmt.bindString(9, startTime);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public DownloadBean readEntity(Cursor cursor, int offset) {
        DownloadBean entity = new DownloadBean( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // url
            cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2), // totalSize
            cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3), // currentSize
            cursor.getInt(offset + 4), // progress
            cursor.getInt(offset + 5), // status
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // filePath
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // fileName
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8) // startTime
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DownloadBean entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setUrl(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setTotalSize(cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2));
        entity.setCurrentSize(cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3));
        entity.setProgress(cursor.getInt(offset + 4));
        entity.setStatus(cursor.getInt(offset + 5));
        entity.setFilePath(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setFileName(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setStartTime(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(DownloadBean entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(DownloadBean entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(DownloadBean entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
