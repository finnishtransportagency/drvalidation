var jqgrid = require('free-jqgrid');
var ajaxrequest = require('./ajaxrequest.js');

var gridController = {
	initValidationGrid: function() {
		var me = this;
		jQuery("#grid2").jqGrid({//http://www.trirand.com/jqgridwiki/doku.php?id=wiki:jqgridexamples
			colModel:[ {name:'tietolaji', label:'Tietolaji', index:'id', width:200},
			           {name:'tyyppi', label:'Tyyppi', width:100},
			           {name:'arvot', label:'Arvot', width:100},
			           {name:'muiden_arvojen_vaikutus', label:'Muiden arvojen vaikutus', width:160},
			           {name:'huom', label:'Huomioitavaa', width:250},
			           {name: 'status', label: 'Status', width:100},
			           {name: 'action', label: 'Validointi', width:120}],
			           width: 1200,
			           url:'validate/rules',
			           datatype: "json",
			           iconSet: "fontAwesome",
			           guiStyle: "bootstrap",
			           rowNum: 10,// rowNum, Sets how many records we want to view in the grid.
			           rowList:[10,20,30],//array to construct a select box element in the pager
			           sortname: 'id',//sets the initial sorting column.
			           viewrecords: true,
			           sortorder: "desc",
			           loadonce: true,
			           caption: "Validointi",
			           subGrid: true,
			           pager: true,//defines that we want to use a pager bar
//			           subGridOptions: {expandOnLoad: true },
			           beforeSelectRow: function (rowid, e) {
			        	    var $td = $(e.target).closest("td"),
			        	        iCol = $.jgrid.getCellIndex($td[0]),
			        	        onCellSelect = $(this).jqGrid("getGridParam", "onCellSelect");

			        	    if ($.isFunction(onCellSelect)) {
			        	        onCellSelect.call(this, rowid, iCol, $td.html(), e);
			        	    }

			        	    return false;
			        	},
			           onCellSelect: me.cellSelected,
			           subGridRowExpanded: function (subgridId, rowid) {
			        	   me.cellSelected(rowid, 7);
			               var subgridTableId = subgridId + "_t";
			               $("#" + subgridId).html("<table id='" + subgridTableId + "'></table>");
			               $("#" + subgridTableId).jqGrid({
			                   datatype: "local",
//			                   data: $(this).jqGrid("getLocalRow", rowid).files,
			                   iconSet: "fontAwesome",
					           guiStyle: "bootstrap",
					           caption: "Validoinnin tulokset",
			                   colNames: ["Parametri", "Arvo"],
			                   colModel: [
			                     {name: "param", width: 130, key: true},
			                     {name: "value", width: 130}
			                   ],
			                   height: "100%",
			                   rowNum: 10,
			                   sortname: "_param",
			                   idPrefix: "s_" + rowid + "_",
			                   onHeaderClick: function(gridstate) { jQuery("#grid2").collapseSubGridRow(rowid) }
			               });
			           }
		})
	},
	getTemplate: function() {
		var me = this;
		return me.escapeHtml('<div class="tooltip userconftooltip"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>');
	},
	escapeHtml: function(str) {
		return String(str).replace(/[&<>"'\/]/g, function (s) {
			var entityMap = {
				    "&": "&amp;",
				    "<": "&lt;",
				    ">": "&gt;",
				    '"': '&quot;',
				    "'": '&#39;',
				    "/": '&#x2F;'
			    	  };
			  return entityMap[s];
			});
	},
	parseRole: function(cellvalue, options, rowObject) {
    	var configurationJSON = JSON.parse(cellvalue);
    	switch(configurationJSON.roles[0]) {
        case undefined:
            return "Pysäkkikäyttäjä";
        case "premium":
        	return "Muokkaaja";
        case "operator":
            return "Operaattori";
        case "viewer":
        	return "Katselija (poistettu rooli)";
        case "busStopMaintainer":
        	return "ELY-pysäkkikäyttäjä";
        default:
            return "n/a";
    	} 
    },
    parseMunicipalities: function(cellvalue, options, rowObject) {
    	var configurationJSON = JSON.parse(cellvalue);
    	return configurationJSON.authorizedMunicipalities;
    },
    cellSelected: function(rowId, iCell, cellcontent, e) {
    	if (iCell == 7) {
    		jQuery("#grid2").jqGrid('setCell', rowId, iCell, 'Validointi aloitettu..','validred');
    		$('.loading').show();
    		//var type = jQuery("#grid2").jqGrid('getCell', rowId, 'tyyppi')
    		ajaxrequest.get("validate/result/" + rowId, "", gridController.updateStatus );
    	}
    },
    cutStringOnComma: function(str, cutcount) {// ei käytössä
		  for(var i = 0; i < str.length; i++) {
			  if (i > cutcount && str[i] == ',') return str.substring(0, i) + " " + str.substr(i + 1);
		  }
		  return str;
	},
	updateStatus: function(response) {
		jQuery("#grid2").jqGrid('setCell', response.rowid, 'action', 'Validointi valmis!', 'validgreen');
		jQuery("#grid2").jqGrid('setCell', response.rowid, 'status', response.status);
		jQuery("#grid2").expandSubGridRow(response.rowid);
//		var x = [];
//		x.push({ _param: "Pienin arvo", _value: "350" });
//		x.push({ _param: "Suurin arvo", _value: "70000" });
		var subgridTableId = "grid2_" + response.rowid + "_t";
		jQuery('#' + subgridTableId).jqGrid('setGridParam', { data: response.params });
		jQuery('#' + subgridTableId).trigger('reloadGrid');
		$('.loading').hide();
	}
}

module.exports = gridController;