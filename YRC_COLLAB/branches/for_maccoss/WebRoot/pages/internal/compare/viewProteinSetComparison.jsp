
<%@page import="org.yeastrc.www.compare.ProteinComparisonDataset"%>
<%@page import="org.yeastrc.www.compare.DatasetColor"%>
<%@page import="org.yeastrc.www.compare.Dataset"%><%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>


<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>


<%@ include file="/includes/header.jsp" %>

<%@ include file="/includes/errors.jsp" %>

<bean:define name="comparison" id="comparison" type="org.yeastrc.www.compare.ProteinComparisonDataset"></bean:define>

<script src="<yrcwww:link path='js/jquery.ui-1.6rc2/jquery-1.2.6.js'/>"></script>
<script src="<yrcwww:link path='js/jquery.form.js'/>"></script>
<script src="<yrcwww:link path='js/jquery.blockUI.js'/>"></script>

<script>
// ---------------------------------------------------------------------------------------
// AJAX DEFAULTS
// ---------------------------------------------------------------------------------------
  $.ajaxSetup({
  	type: 'POST',
  	//timeout: 5000,
  	dataType: 'html',
  	error: function(xhr) {
  			
  				var statusCode = xhr.status;
		  		// status code returned if user is not logged in
		  		// reloading this page will redirect to the login page
		  		if(statusCode == 303)
 					window.location.reload();
 				
 				// otherwise just display an alert
 				else {
 					alert("Request Failed: "+statusCode+"\n"+xhr.statusText);
 				}
  			}
  });
  
  $.blockUI.defaults.message = '<b>Loading...</b>'; 
  $.blockUI.defaults.css.padding = 20;
  $.blockUI.defaults.fadeIn = 0;
  $.blockUI.defaults.fadeOut = 0;
  $().ajaxStop($.unblockUI);
  
// ---------------------------------------------------------------------------------------
// SETUP THE TABLE
// ---------------------------------------------------------------------------------------
$(document).ready(function() {
	
    
   $("#compare_results_pager").attr('width', "80%").attr('align', 'center');
   
   var colCount = <%=comparison.tableHeaders().size()%>
   $("#compare_results").each(function() {
   		var $table = $(this);
   		$table.attr('width', "80%");
   		$table.attr('align', 'center');
   		$('.prot_descr', $table).css("font-size", "8pt");
   		
   		
   		$('.pept_count', $table).each(function() {
   		
   			var nrseqId = $(this).attr('id');
   			$(this).addClass('pept_closed');
   			
   			$(this).click(function() {
   				// append a row for the peptide list to go into
   				var row = $(this).parent();
   				if($(this).is('.pept_closed')) {
   					$(this).removeClass('pept_closed');
   					$(this).addClass('pept_open');
   					
   					if($(this).is('.has_peptides')) {
   						$(row).next().show();
   					}
   					else {
   						// append a row
   						var newRow = "<tr><td colspan='"+colCount+"'>";
   						newRow += "<div align='center' width='90%' id='peptides_"+nrseqId+"'></div></td></tr>"
   						$(row).after(newRow);
   						
   						// send a request for the peptides
   						$.blockUI();
  						$("#peptides_"+nrseqId).load("doPeptidesComparison.do", 	//url
  											{'piDatasetIds': 	'<bean:write name="piDatasetIds"/>', 	// data
  									 		 'dtaDatasetIds':   '<bean:write name="dtaDatasetIds"/>',
  									 		 'nrseqProteinId': 		nrseqId
  									 },
  									 function(responseText, status, xhr) {			// callback
  									 	$.unblockUI();
  									 	$("#peptides_"+nrseqId).addClass('has_peptides');
  									 	// make the table sortable
  									 	setupPeptidesTable($('#peptides_table_'+nrseqId));
  								   });
   					}
   				}
   				else {
   					$(this).removeClass('pept_open');
   					$(this).addClass('pept_closed');
   					$(row).next().hide();
   				}
   			});
   			
   		});
   		
   		
   		<%for(int i = 0; i < comparison.getDatasetCount(); i++) {
   			String color = DatasetColor.get(i).R+", "+DatasetColor.get(i).G+","+DatasetColor.get(i).B;
   		%>
   			$("td.prot-found[id="+<%=String.valueOf(i)%>+"]", $table).css('background-color', "rgb(<%=color%>)");
   		<%}%>
   		
   		$('td.prot-parsim', $table).css('color', 'white').css('font-weight', '#FFFFFF');
   		
   		//$('td.prot-parsim', $table).css('background-color', 'red');
   		
   		//$('tbody > tr:odd', $table).addClass("tr_odd");
   		//$('tbody > tr:even', $table).addClass("tr_even");
   });
});

// ---------------------------------------------------------------------------------------
// SETUP THE PEPTIDES TABLE
// ---------------------------------------------------------------------------------------
function  setupPeptidesTable(table){
		var $table = $(table);
   		$table.attr('width', "60%");
   		$table.attr('align', 'center');
   		$table.css("margin", "5 5 5 5");
   		
   		<%for(int i = 0; i < comparison.getDatasetCount(); i++) {
   			String color = DatasetColor.get(i).R+", "+DatasetColor.get(i).G+","+DatasetColor.get(i).B;
   		%>
   			$("td.pept-found[id="+<%=String.valueOf(i)%>+"]", $table).css('background-color', "rgb(<%=color%>)");
   		<%}%>
   		
   		$('td.pept-unique', $table).css('color', 'white').css('font-weight', '#FFFFFF');
   		
}

// ---------------------------------------------------------------------------------------
// PAGE RESULTS
// ---------------------------------------------------------------------------------------
function pageResults(pageNum) {
  	$("input#pageNum").val(pageNum);
  	//alert("setting to "+pageNum+" value set to: "+$("input#pageNum").val());
  	$("form[name='proteinSetComparisonForm']").submit();
}

// ---------------------------------------------------------------------------------------
// UPDATE RESULTS
// ---------------------------------------------------------------------------------------
function updateResults() {
  	$("input#pageNum").val(1);
  	$("form[name='proteinSetComparisonForm']").submit();
}

// ---------------------------------------------------------------------------------------
// TOGGLE AND, OR, NOT FILTERS
// ---------------------------------------------------------------------------------------
var colors = [];
<%
	int datasetCount = comparison.getDatasetCount();
	for(int i = 0; i < datasetCount; i++) {
%>
	colors[<%=i%>] = '<%="rgb("+DatasetColor.get(i).R+","+DatasetColor.get(i).G+","+DatasetColor.get(i).B+")"%>';
<%
}
%>
function toggleAndSelect(dsIndex) {
	var id = "AND_"+dsIndex+"_select";
	var value = $("input#"+id).val();
	
	if(value == "true") {
		$("input#"+id).val("false");	
		$("td#AND_"+dsIndex+"_td").css("background-color", "#FFFFFF");
	}
	else {
		$("input#"+id).val("true");	
		$("td#AND_"+dsIndex+"_td").css("background-color", colors[dsIndex]);
	}
}
function toggleOrSelect(dsIndex) {
	var id = "OR_"+dsIndex+"_select";
	var value = $("input#"+id).val();
	if(value == "true") {
		$("input#"+id).val("false");	
		$("td#OR_"+dsIndex+"_td").css("background-color", "#FFFFFF");
	}
	else {
		$("input#"+id).val("true");	
		$("td#OR_"+dsIndex+"_td").css("background-color", colors[dsIndex]);
	}
}
function toggleNotSelect(dsIndex) {
	var id = "NOT_"+dsIndex+"_select";
	var value = $("input#"+id).val();
	if(value == "true") {
		$("input#"+id).val("false");	
		$("td#NOT_"+dsIndex+"_td").css("background-color", "#FFFFFF");
	}
	else {
		$("input#"+id).val("true");	
		$("td#NOT_"+dsIndex+"_td").css("background-color", colors[dsIndex]);
	}
}
</script>

<CENTER>




<yrcwww:contentbox centered="true" width="90" widthRel="true" title="Protein Dataset Comparison">

<table align="center">

<tr>
<td colspan="2" style="background-color:#F2F2F2; font-weight:bold; text-align: center; padding:5 5 5 5;" >
Total Proteins: <bean:write name="comparison" property="totalProteinCount" />
</td>

<logic:present name="chart">
<td rowspan="5">
	<img src="<bean:write name='chart' />" align="top" alt="Comparison"></img>
</td>
</logic:present>

</tr>

<tr valign="top">
<td>
<table  class="table_basic">
<thead>
<tr>
<th>&nbsp;</th>
<logic:iterate name="comparison" property="datasets" id="dataset" indexId="column">
	<th>Dataset ID<bean:write name="dataset" property="datasetId"/></th>
</logic:iterate>
</tr>
</thead>

<tbody>
<logic:iterate name="comparison" property="datasets" id="dataset" indexId="row">
<tr>
<th style="padding-right:10;" >Dataset ID <bean:write name="dataset" property="datasetId"/></th>


<logic:iterate name="comparison" property="datasets" id="dataset" indexId="column">
	
	<logic:equal name="column" value="<%=String.valueOf(row)%>">
		<td style="background-color: rgb(<%=DatasetColor.get(row).R %>,<%=DatasetColor.get(row).G %>,<%=DatasetColor.get(row).B %> );">
		&nbsp;
	</td>
	</logic:equal>
	
	<logic:notEqual name="column" value="<%=String.valueOf(row)%>">
		<td>(<%=comparison.getCommonProteinCount(row, column) %>)&nbsp;<%=comparison.getCommonProteinsPerc(row, column) %>%</td>
	</logic:notEqual>
</logic:iterate>

</tr>
</logic:iterate>
</tbody>
</table>

</td>
<td>
<table class="table_basic">
<thead>
	<tr>
		<th>Dataset</th>
		<th># Proteins</th>
	</tr>
</thead>
<tbody>
<logic:iterate name="comparison" property="datasets" id="dataset" indexId="row">
	<tr>
		<td align="center"
		    style="color:#FFFFFF; background-color: rgb(<%=DatasetColor.get(row).R %>,<%=DatasetColor.get(row).G %>,<%=DatasetColor.get(row).B %> ); padding: 4 5 3 5;">
			<span>ID <bean:write name="dataset" property="datasetId" /></span>
		</td>
		<td><%=comparison.getProteinCount(row)%></td>
	</tr>
</logic:iterate>
</tbody>
</table>
</td>
</tr>


</table>

<br>


<!-- ################## FILTER FORM  ########################################### -->
<html:form action="doProteinSetComparison" method="POST">

	<logic:iterate name="proteinSetComparisonForm" property="proteinferRunList" id="proteinferRun">
		<logic:equal name="proteinferRun" property="selected" value="true">
			<html:hidden name="proteinferRun" property="runId" indexed="true" />
			<html:hidden name="proteinferRun" property="selected" indexed="true" />
		</logic:equal>
	</logic:iterate>
	
	<logic:iterate name="proteinSetComparisonForm" property="dtaRunList" id="dtaRun">
		<logic:equal name="dtaRun" property="selected" value="true">
			<html:hidden name="dtaRun" property="runId" indexed="true" />
			<html:hidden name="dtaRun" property="selected" indexed="true" />
		</logic:equal>
	</logic:iterate>
	
	<html:hidden name="proteinSetComparisonForm" property="pageNum" styleId="pageNum" />


<center>
<br>

<div style="background-color:#F0F8FF; padding: 5 0 5 0; border: 1px solid gray; width:80%">
<table>
	<tr>
		<td style="padding-right:10;"><b>Filter:</b></td>
		<td><b>AND</b></td>
		<td>
			<table cellpadding="0" cellspacing="0">
			<tr>
				<logic:iterate name="proteinSetComparisonForm" property="andList" id="andDataset" indexId="dsIndex">
					
					<logic:equal name="andDataset" property="selected" value="true">
						<td style="background-color:rgb(<%=DatasetColor.get(dsIndex).R %>,<%=DatasetColor.get(dsIndex).G %>,<%=DatasetColor.get(dsIndex).B %>); border:1px solid #AAAAAA;"
							id='<%="AND_"+dsIndex+"_td"%>'
						>
					</logic:equal>
					<logic:notEqual name="andDataset" property="selected" value="true">
						<td style="background-color:#FFFFFF; border:1px solid #AAAAAA;" id='<%="AND_"+dsIndex+"_td"%>' >
					</logic:notEqual>
					<span style="cursor:pointer;" onclick="javascript:toggleAndSelect(<%=dsIndex %>);">&nbsp;&nbsp;</span>
					<html:hidden name="andDataset" property="datasetId" indexed="true" />
					<html:hidden name="andDataset" property="sourceString" indexed="true" />
					<html:hidden name="andDataset" property="selected" indexed="true" styleId='<%="AND_"+dsIndex+"_select"%>' />
					</td>
				</logic:iterate>
			</tr>
			</table>
		</td>
		<td>&nbsp;&nbsp;&nbsp;</td>
		<td><b>OR</b></td>
		<td>
			<table  cellpadding="0" cellspacing="0">
			<tr>
				<logic:iterate name="proteinSetComparisonForm" property="orList" id="orDataset" indexId="dsIndex">
					
					<logic:equal name="orDataset" property="selected" value="true">
						<td style="background-color:rgb(<%=DatasetColor.get(dsIndex).R %>,<%=DatasetColor.get(dsIndex).G %>,<%=DatasetColor.get(dsIndex).B %>); border:1px solid #AAAAAA;"
							id='<%="OR_"+dsIndex+"_td"%>'
						>
					</logic:equal>
					<logic:notEqual name="orDataset" property="selected" value="true">
						<td style="background-color:#FFFFFF; border:1px solid #AAAAAA;" id='<%="OR_"+dsIndex+"_td"%>'>
					</logic:notEqual>
					<span style="cursor:pointer;" onclick="javascript:toggleOrSelect(<%=dsIndex %>);">&nbsp;&nbsp;</span>
					<html:hidden name="orDataset" property="datasetId" indexed="true" />
					<html:hidden name="orDataset" property="sourceString" indexed="true" />
					<html:hidden name="orDataset" property="selected" indexed="true" styleId='<%="OR_"+dsIndex+"_select"%>' />
					</td>
				</logic:iterate>
			</tr>
			</table>
		</td>
		<td>&nbsp;&nbsp;&nbsp;</td>
		<td><b>NOT</b></td>
		<td>
			<table  cellpadding="0" cellspacing="0">
			<tr>
				<logic:iterate name="proteinSetComparisonForm" property="notList" id="notDataset" indexId="dsIndex">
					
					<logic:equal name="notDataset" property="selected" value="true">
						<td style="background-color:rgb(<%=DatasetColor.get(dsIndex).R %>,<%=DatasetColor.get(dsIndex).G %>,<%=DatasetColor.get(dsIndex).B %>); border:1px solid #AAAAAA;"
							id='<%="NOT_"+dsIndex+"_td"%>'
						>
					</logic:equal>
					<logic:notEqual name="notDataset" property="selected" value="true">
						<td style="background-color:#FFFFFF; border:1px solid #AAAAAA;" id='<%="NOT_"+dsIndex+"_td"%>'>
					</logic:notEqual>
					<span style="cursor:pointer;" onclick="javascript:toggleNotSelect(<%=dsIndex %>);">&nbsp;&nbsp;</span>
					<html:hidden name="notDataset" property="datasetId" indexed="true" />
					<html:hidden name="notDataset" property="sourceString" indexed="true" />
					<html:hidden name="notDataset" property="selected" indexed="true" styleId='<%="NOT_"+dsIndex+"_select"%>' />
					</td>
					
				</logic:iterate>
			</tr>
			</table>
		</td>
		<td style="padding-left:10">
			<html:submit value="Update" onclick="javascript:updateResults();"></html:submit>
		</td>
		<td style="padding-left:20">
			# Filtered Proteins: <bean:write name="comparison" property="filteredProteinCount"/>
		</td>
		
	</tr>
</table>
</div>
</center>
</html:form>

<br>

<!-- PAGE RESULTS -->
<bean:define name="comparison" id="pageable" />
<table id="compare_results_pager">
<tr>
<td>
<%@include file="/pages/internal/pager.jsp" %>
</td>
</tr>
</table>
		
<!-- RESULTS TABLE -->
<div > 
<yrcwww:table name="comparison" tableId='compare_results' tableClass="table_basic sortable_table" center="true" />
</div>

</yrcwww:contentbox>

</CENTER>

<%@ include file="/includes/footer.jsp" %>