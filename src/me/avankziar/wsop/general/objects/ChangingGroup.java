package me.avankziar.wsop.general.objects;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.logging.Level;

import me.avankziar.wsop.general.database.MysqlBaseHandler;
import me.avankziar.wsop.general.database.MysqlBaseSetup;
import me.avankziar.wsop.general.database.MysqlTable;
import me.avankziar.wsop.general.database.QueryType;
import me.avankziar.wsop.general.database.ServerType;

/**
 * Example Object
 * @author User
 *
 */
public class ChangingGroup implements MysqlTable<ChangingGroup>
{
	private long id;
	private UUID uuid;
	private String name;
	private String changedGroup;
	private String context;
	private boolean primaryGroup;
	
	public ChangingGroup(){}
	
	public ChangingGroup(long id, UUID uuid, String name, String changedGroup, boolean primaryGroup, String... context)
	{
		setId(id);
		setUUID(uuid);
		setName(name);
		setChangedGroup(changedGroup);
		setPrimaryGroup(primaryGroup);
		setContext(String.join(";", context));
	}
	
	public ServerType getServerType()
	{
		return ServerType.ALL;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}
	
	public UUID getUUID()
	{
		return uuid;
	}

	public void setUUID(UUID uuid)
	{
		this.uuid = uuid;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getChangedGroup() {
		return changedGroup;
	}

	public void setChangedGroup(String changedGroup) {
		this.changedGroup = changedGroup;
	}
	
	public boolean isPrimaryGroup() {
		return primaryGroup;
	}

	public void setPrimaryGroup(boolean primaryGroup) {
		this.primaryGroup = primaryGroup;
	}

	public String getContext()
	{
		return context;
	}
	
	public LinkedHashMap<String, String> getContextMap()
	{
		if(context == null)
		{
			return null;
		}
		String[] a = context.split(";");
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		for(String split : a)
		{
			String[] s = split.split("=");
			if(s.length != 2)
			{
				continue;
			}
			map.put(s[0], s[1]);
		}
		return map;
	}

	public void setContext(String context)
	{
		this.context = context;
	}

	public String getMysqlTableName()
	{
		return "wsopPlayerData";
	}
	
	public boolean setupMysql(MysqlBaseSetup mysqlSetup, ServerType serverType)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE IF NOT EXISTS `"+getMysqlTableName()
				+ "` (id bigint AUTO_INCREMENT PRIMARY KEY,"
				+ " player_uuid char(36) NOT NULL,"
				+ " player_name varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,"
				+ " changing_groups text,"
				+ " primary_group boolean,"
				+ " contextset text);");
		return mysqlSetup.baseSetup(sql.toString());
	}

	@Override
	public boolean create(Connection conn)
	{
		try
		{
			String sql = "INSERT INTO `" + getMysqlTableName()
					+ "`(`player_uuid`, `player_name`, `changing_groups`, `primary_group`, `contextset`) " 
					+ "VALUES(?, ?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
	        ps.setString(1, getUUID().toString());
	        ps.setString(2, getName());
	        ps.setString(3, getChangedGroup());
	        ps.setBoolean(4, isPrimaryGroup());
	        ps.setString(5, getContext());
	        int i = ps.executeUpdate();
	        MysqlBaseHandler.addRows(QueryType.INSERT, i);
	        return true;
		} catch (SQLException e)
		{
			this.log(MysqlBaseHandler.getLogger(), Level.WARNING, "SQLException! Could not create a "+this.getClass().getSimpleName()+" Object!", e);
		}
		return false;
	}

	@Override
	public boolean update(Connection conn, String whereColumn, Object... whereObject)
	{
		try
		{
			String sql = "UPDATE `" + getMysqlTableName()
				+ "` SET `player_uuid` = ?, `player_name` = ?, `changing_groups` = ?, `primary_group` = ?, `contextset` = ?" 
				+ " WHERE "+whereColumn;
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, getUUID().toString());
			ps.setString(2, getName());
			ps.setString(3, getChangedGroup());
	        ps.setBoolean(4, isPrimaryGroup());
	        ps.setString(5, getContext());
			int i = 6;
			for(Object o : whereObject)
			{
				ps.setObject(i, o);
				i++;
			}			
			int u = ps.executeUpdate();
			MysqlBaseHandler.addRows(QueryType.UPDATE, u);
			return true;
		} catch (SQLException e)
		{
			this.log(MysqlBaseHandler.getLogger(), Level.WARNING, "SQLException! Could not update a "+this.getClass().getSimpleName()+" Object!", e);
		}
		return false;
	}

	@Override
	public ArrayList<ChangingGroup> get(Connection conn, String orderby, String limit, String whereColumn, Object... whereObject)
	{
		try
		{
			String sql = "SELECT * FROM `" + getMysqlTableName()
				+ "` WHERE "+whereColumn+" ORDER BY "+orderby+limit;
			PreparedStatement ps = conn.prepareStatement(sql);
			int i = 1;
			for(Object o : whereObject)
			{
				ps.setObject(i, o);
				i++;
			}
			
			ResultSet rs = ps.executeQuery();
			MysqlBaseHandler.addRows(QueryType.READ, rs.getMetaData().getColumnCount());
			ArrayList<ChangingGroup> al = new ArrayList<>();
			while (rs.next()) 
			{
				al.add(new ChangingGroup(rs.getInt("id"),
						UUID.fromString(rs.getString("player_uuid")),
						rs.getString("player_name"),
						rs.getString("changing_groups"),
						rs.getBoolean("primary_group"),
						rs.getString("contextset")));
			}
			return al;
		} catch (SQLException e)
		{
			this.log(MysqlBaseHandler.getLogger(), Level.WARNING, "SQLException! Could not get a "+this.getClass().getSimpleName()+" Object!", e);
		}
		return new ArrayList<>();
	}
}