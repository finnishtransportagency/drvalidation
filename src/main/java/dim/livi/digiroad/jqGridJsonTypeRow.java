package dim.livi.digiroad;

import java.util.List;

public class jqGridJsonTypeRow {
	
	private String id;
	private List<String> cell;
	private String sql;
	
	public jqGridJsonTypeRow(String id, List<String> cell, String sql) {
		this.id = id;
		this.cell = cell;
		this.sql = sql;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public List<String> getCell() {
		return cell;
	}
	public void setCell(List<String> cell) {
		this.cell = cell;
	}

}
