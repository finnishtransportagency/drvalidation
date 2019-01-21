package dim.livi.digiroad.reporttool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dim.livi.digiroad.NisRepository;
import dim.livi.digiroad.ParamValue;
import dim.livi.digiroad.Utilities;
import dim.livi.digiroad.ValidationResult;
import dim.livi.digiroad.jqGridJsonType;
import dim.livi.digiroad.jqGridJsonTypeRow;

@RestController
public class ValidationController {
	
	@Autowired 
	private NisRepository items;
	
	@RequestMapping("/validate")
	public Integer validate(@RequestParam int id) {		
		Integer[] typelist = new Integer[]{1,3};
		return items.getValidityManoeuvreCount(typelist, Utilities.sql.IN.toString());
	}
	
	@RequestMapping("/validate/rules")
	public ResponseEntity<jqGridJsonType> getValidationRules() {
		return new ResponseEntity<jqGridJsonType>(new jqGridJsonType(0, 0, 0, items.getValidationRules(0)), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/validate/result/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<ValidationResult> result(@PathVariable Integer id, HttpSession session) throws InterruptedException, ExecutionException {
		jqGridJsonTypeRow validationRule = items.getValidationRules(id).get(0);
		String type = null != validationRule.getCell().get(1) ? validationRule.getCell().get(1) : "undefined";
		String filter = null != validationRule.getCell().get(2) ? validationRule.getCell().get(2) : "=0";
		Integer Asset_type_id = Integer.parseInt(validationRule.getCell().get(5));
		String Sql = null != validationRule.getSql() ? validationRule.getSql() : "undefined";
		if ("number".equals(type)) {
			final Future<List<ParamValue>> future = items.getValidationResult(Asset_type_id, filter, Sql);
			while (!future.isDone()) {Thread.sleep(1000L);}
			ValidationResult valRes = new ValidationResult("ok", id, future.get());
			return new ResponseEntity<ValidationResult>(valRes, HttpStatus.OK);
		} else {
			final Future<List<String>> future = items.getSleep(id);
			while (!future.isDone()) {Thread.sleep(1000L);}
			@SuppressWarnings("serial")
			ValidationResult valRes = new ValidationResult("fail", id, (List<ParamValue>) new ArrayList<ParamValue>(){{add(new ParamValue("Fake", "007"));}});
			return new ResponseEntity<ValidationResult>(valRes, HttpStatus.OK);
		}
	}
}
