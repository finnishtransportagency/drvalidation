package dim.livi.digiroad;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Repository;


@Repository
public class NisRepository {
	
	
	protected JdbcTemplate jdbc;
	
	@Autowired
    public NisRepository(JdbcTemplate jdbc) {
        this.jdbc=jdbc;
    } 

	 public int getValidityManoeuvreCount(Integer[] typelist, String ToBeOrNotToBe) {
		 String in = ToBeOrNotToBe + " (" + StringUtils.join(typelist, ',') + ")";
	        return jdbc.queryForObject("select count(*) count from DR2USER.MANOEUVRE_ELEMENT me " +
										"inner join DR2USER.MANOEUVRE m on me.MANOEUVRE_ID = m.ID " +
										"where m.VALID_TO is not null and ELEMENT_TYPE " + in, itemMapper ); 
	    }
	 
	@Async
	public Future<List<String>> getSleep(Integer id) throws InterruptedException {
		Thread.sleep(3000L);
		List<String> lista = new ArrayList<String>();
		lista.add("ok");
		lista.add(id.toString());
		return new AsyncResult<List<String>>(lista);
	}
	 
	public List<jqGridJsonTypeRow> getValidationRules(Integer id) {
		String whereClause = "1 = 1";
		if (id != 0) whereClause = "VR.ID=" + id;
		return jdbc.query("select VR.ID as ID, VR.TIETOLAJI as TIETOLAJI, VR.TYYPPI as TYYPPI, VR.ARVOT as ARVOT, "
				+ "VR.MUIDEN_ARVOJEN_VAIKUTUS as MUIDEN_ARVOJEN_VAIKUTUS, VR.HUOM as HUOM, VR.ASSET_TYPE_ID as ASSET_TYPE_ID, VS.SQL as SQL "
				+ "from (OPERAATTORI.VALIDATION_RULES VR "
				+ "inner join OPERAATTORI.VALIDATION_SQL VS ON (VS.ID = VR.TEMP_TABLE_SQL_ID)) "
				+ "WHERE " + whereClause, new Object[]{}, new RowMapperResultSetExtractor<jqGridJsonTypeRow>(validationRuleMapper));
	 }
	/*public List<jqGridJsonTypeRow> getValidationRules(Integer id) {
		String whereClause = "1 = 1";
		if (id != 0) whereClause = "ID=" + id;
		return jdbc.query("select ID,TIETOLAJI, TYYPPI, ARVOT, MUIDEN_ARVOJEN_VAIKUTUS,HUOM, ASSET_TYPE_ID from OPERAATTORI.VALIDATION_RULES WHERE " + whereClause, new Object[]{}, new RowMapperResultSetExtractor<jqGridJsonTypeRow>(validationRuleMapper));
	 }*/
	
	@Async
	public Future<List<ParamValue>> getValidationResult(Integer asset_type_id, String filter, String sql) {
		String kysely = "with arvot as ("+ sql +") " +
				"select 'c' porder, 'Pienin arvo' param, min(value) value from arvot " +
				"union " +
				"select 'c' porder, 'Suurin arvo', max(value) from arvot " +
				"union " +
				"select case when count(value) " + filter + " then 'a' else 'b' end porder, param, count(value) from (" +
				  "select case when value " + filter + " then 'Valideja' else 'Ei valideja' end param, case when value " + filter + " then 1 else 0 end value from arvot) " +
				"group by param, value " +
				"order by porder";
		if (asset_type_id == 99){
			return new AsyncResult<List<ParamValue>>(jdbc.query(kysely, new RowMapperResultSetExtractor<ParamValue>(ParamValueMapper)));
			}
		else {
			return new AsyncResult<List<ParamValue>>(jdbc.query(kysely, new Object[]{asset_type_id}, new RowMapperResultSetExtractor<ParamValue>(ParamValueMapper)));
		}
		}

	    private static final RowMapper<Integer> itemMapper = new RowMapper<Integer>() {
	        @Override
	        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException { 
	            int item = rs.getInt("count");
	            return item;
			} 
	    };
	    
	    private static final RowMapper<jqGridJsonTypeRow> validationRuleMapper = new RowMapper<jqGridJsonTypeRow>() {
	        @Override
	        public jqGridJsonTypeRow mapRow(ResultSet rs, int rowNum) throws SQLException {
	        	
	        	jqGridJsonTypeRow result = new jqGridJsonTypeRow(String.valueOf(rs.getInt("ID")), Arrays.asList(rs.getString("TIETOLAJI"), rs.getString("TYYPPI"),
	        			rs.getString("ARVOT"), rs.getString("MUIDEN_ARVOJEN_VAIKUTUS"), rs.getString("HUOM"), String.valueOf(rs.getInt("ASSET_TYPE_ID"))), rs.getString("SQL"));
	        	 return result;
			}
	        
	    };
	    
	    private static final RowMapper<ParamValue> ParamValueMapper = new RowMapper<ParamValue>() {
	        @Override
	        public ParamValue mapRow(ResultSet rs, int rowNum) throws SQLException {
	        	return new ParamValue(rs.getString("PARAM"), rs.getString("VALUE"));
			} 
	    };
	    

}
