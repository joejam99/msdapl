
<%@page import="org.yeastrc.www.compare.ProteinComparisonDataset"%>
<%@page import="org.yeastrc.www.compare.dataset.DatasetColor"%>
<%@page import="org.yeastrc.www.compare.dataset.Dataset"%>
<%@page import="org.yeastrc.www.compare.dataset.DatasetSource"%>
<%@page import="org.yeastrc.ms.domain.search.SORT_ORDER"%><%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>


<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>

<logic:notPresent name="comparison">
	<logic:forward name="doProteinSetComparison" />
</logic:notPresent>

<%@ include file="/includes/header.jsp" %>

<%@ include file="/includes/errors.jsp" %>

<bean:define name="comparison" id="comparison" type="org.yeastrc.www.compare.ProteinComparisonDataset"></bean:define>

<script src="<yrcwww:link path='js/jquery.ui-1.6rc2/jquery-1.2.6.js'/>"></script>
<script src="<yrcwww:link path='js/jquery.form.js'/>"></script>
<script src="<yrcwww:link path='js/jquery.blockUI.js'/>"></script>

<%@include file="comparisonFunctions.jsp" %>
<script>

// ---------------------------------------------------------------------------------------
// INITIALIZE
// ---------------------------------------------------------------------------------------
$(document).ready(function() {
	
   var colCount = <%=comparison.tableHeaders().size()%>
   $("#compare_results").each(function() {
   		var $table = $(this);
   		$table.attr('width', "95%");
   		$table.attr('align', 'center');
   		$('.prot_descr', $table).css("font-size", "8pt");
   		
   		
   		// ---------------------------------------------------------------
   		// LINK TO GROUP PROTEINS
   		$('.prot-group', $table).each(function() {
   		
   			var nrseqId = $(this).attr('name');
   			var row = $(this).parent();
   			$(row).addClass('prot_closed');
   			
   			$(this).click(function() {
   				
   				var cell = $(this);
   				if($(row).is('.prot_closed')) {
   					$(row).removeClass('prot_closed');
   					$(row).addClass('prot_open');
   					
   					if($(row).is('.has_proteins')) {
   						$(row).next().show();
   					}
   					else {
   						// append a row for the protein groups to go into
   						var newRow = "<tr><td colspan='"+colCount+"'>";
   						newRow += "<div align='center' width='90%' id='proteins_"+nrseqId+"'></div></td></tr>"
   						$(row).after(newRow);
   						
   						// send a request for the peptides
   						$.blockUI();
  						$("#proteins_"+nrseqId).load("<yrcwww:link path='doProteinGroupComparison.do'/>", 	//url
  											{'datasetIds': 	'<bean:write name="datasetIds"/>', 	// data
  									 		 'nrseqProteinId': 		nrseqId
  									 },
  									 function(responseText, status, xhr) {			// callback
  									 	$.unblockUI();
  									 	$(row).addClass('has_proteins');
  								   });
   					}
   				}
   				else {
   					$(row).removeClass('prot_open');
   					$(row).addClass('prot_closed');
   					$(row).next().hide();
   				}
   			});
   		});
   		                    //cell.setHyperlink("doProteinGroupComparison.do?datasetIds="+getCommaSeparatedDatasetIds()+"&nrseqProteinId="+protein.getNrseqId());
   		
   		
   		// ---------------------------------------------------------------
   		// LINK TO PEPTIDES
   		$('.pept_count', $table).each(function() {
   		
   			var nrseqId = $(this).attr('id');
   			var row = $(this).parent();
   			$(row).addClass('pept_closed');
   			
   			$(this).click(function() {
   				
   				var cell = $(this);
   				if($(row).is('.pept_closed')) {
   					$(row).removeClass('pept_closed');
   					$(row).addClass('pept_open');
   					
   					if($(row).is('.has_peptides')) {
   						if($(row).is('.has_proteins')) {
   							$(row).next().next().show();
   						}
   						else {
   							$(row).next().show();
   						}
   					}
   					else {
   						// append a row for the peptide list to go into
   						var newRow = "<tr><td colspan='"+colCount+"'>";
   						newRow += "<div align='center' width='90%' id='peptides_"+nrseqId+"'></div></td></tr>"
   						
   						if($(row).is('.has_proteins')) {
   							$(row).next().after(newRow);
   						}
   						else {
   							$(row).after(newRow);
   						}
   						
   						
   						// send a request for the peptides
   						$.blockUI();
  						$("#peptides_"+nrseqId).load("<yrcwww:link path='doPeptidesComparison.do'/>", 	//url
  											{'datasetIds': 	'<bean:write name="datasetIds"/>', 	// data
  									 		 'nrseqProteinId': 		nrseqId
  									 },
  									 function(responseText, status, xhr) {			// callback
  									 	$.unblockUI();
  									 	$(row).addClass('has_peptides');
  									 	// make the table sortable
  									 	setupPeptidesTable($('#peptides_table_'+nrseqId));
  								   });
   					}
   				}
   				else {
   					$(row).removeClass('pept_open');
   					$(row).addClass('pept_closed');
   					if($(row).is('.has_proteins')) {
						$(row).next().next().hide();
					}
					else {
						$(row).next().hide();
					}
   				}
   			});
   			
   		});
   		
   });
});

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
		<th align="center">
			<span>
				<logic:equal name="dataset" property="sourceString" value="<%= DatasetSource.PROTEIN_PROPHET.name()%>">
					<html:link action="viewProteinProphetResult.do" paramId="pinferId" paramName="dataset" paramProperty="datasetId">ID <bean:write name="dataset" property="datasetId" /></html:link>
				</logic:equal>
				<logic:notEqual name="dataset" property="sourceString" value="<%= DatasetSource.PROTEIN_PROPHET.name()%>">
					<html:link action="viewProteinInferenceResult.do" paramId="pinferId" paramName="dataset" paramProperty="datasetId">ID <bean:write name="dataset" property="datasetId" /></html:link>
				</logic:notEqual>
			</span>
		</th>
		<td style="color:#FFFFFF; background-color: rgb(<%=DatasetColor.get(row).R %>,<%=DatasetColor.get(row).G %>,<%=DatasetColor.get(row).B %> ); padding: 3 5 3 5;">
			<%=comparison.getProteinCount(row)%>
		</td>
	</tr>
</logic:iterate>
</tbody>
</table>
</td>

<td>
<table  class="table_basic">
<thead>
<tr>
<logic:iterate name="comparison" property="datasets" id="dataset" indexId="column">
	<th>ID<bean:write name="dataset" property="datasetId"/></th>
</logic:iterate>
</tr>
</thead>

<tbody>
<logic:iterate name="comparison" property="datasets" id="dataset" indexId="row">
<tr>

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

</tr>


</table>

<table  align="center" style="border: 1px dashed gray;" width="80%">
<tbody>
<logic:iterate name="comparison" property="datasets" id="dataset" indexId="row">
	<bean:define id="mod" value="<%=String.valueOf(row%2)%>"></bean:define>
	<logic:equal name="mod" value="0"><tr></logic:equal>
	<td width="2%"style="background-color: rgb(<%=DatasetColor.get(row).R %>,<%=DatasetColor.get(row).G %>,<%=DatasetColor.get(row).B %> );">
		&nbsp;&nbsp;
	</td>
	<logic:equal name="dataset" property="sourceString" value="<%= DatasetSource.PROTEIN_PROPHET.name()%>">
		<td style="font-size:8pt;text-align:left;"><html:link action="viewProteinProphetResult.do" paramId="pinferId" paramName="dataset" paramProperty="datasetId">ID <bean:write name="dataset" property="datasetId" /></html:link></td>
	</logic:equal>
	<logic:notEqual name="dataset" property="sourceString" value="<%= DatasetSource.PROTEIN_PROPHET.name()%>">
		<td style="font-size:8pt;text-align:left;"><html:link action="viewProteinInferenceResult.do" paramId="pinferId" paramName="dataset" paramProperty="datasetId">ID <bean:write name="dataset" property="datasetId" /></html:link></td>
	</logic:notEqual>
	
	<td width="42%" style="font-size:8pt;" ><bean:write name="dataset" property="datasetComments" /></td>
	<logic:equal name="mod" value="1"></tr></logic:equal>
</logic:iterate>
</tbody>
</table>

<logic:present name="dtasWarning">
<p style="color:red; font-weight: bold;" align="center">
WARNING:  Comparison with DTASelect results is not yet fully supported. 
</p>
</logic:present>

<!-- ################## FILTER FORM  ########################################### -->
<%@include file="comparisonFilterForm.jsp" %>


<!-- PAGE RESULTS -->
<bean:define name="comparison" id="pageable" />
<table id="compare_results_pager1">
<tr>
<td>
<%@include file="/pages/internal/pager.jsp" %>
</td>
</tr>
<tr>
<td>
<div style="margin:top: 3px;">
<table align="center" width="100%">
<tr>
	<td align="left" valign="bottom">
		<span class="underline clickable" style="font-size:8pt;color:red;" id="full_names" onclick="toggleFullNames()">[Full Names]</span> &nbsp; &nbsp;
		<span class="underline clickable" style="font-size:8pt;color:red;" id="full_descriptions" onclick="toggleFullDescriptions()">[Full Descriptions]</span>
	
		<logic:present name="clusteredImgUrl">
				&nbsp;&nbsp;
				<nobr>
				<b>Heatmap:</b>
				<span style="background-color:yellow;"><a href="JavaScript:newPopup('<bean:write name='clusteredImgUrl'/>');"><b>PDF</b></a></span>
				<logic:present name="dsOrder">
					<span style="background-color:yellow;"><a href="JavaScript:newPopup('<yrcwww:link path='heatmap.do?token='/><bean:write name="proteinSetComparisonForm" property='clusteringToken' />&dsOrder=<bean:write name="dsOrder" />&gradient=<bean:write name="gradient"/>');"><b>HTML</b></a></span>
				</logic:present>
				<logic:notPresent name="dsOrder">
					<span style="background-color:yellow;"><a href="JavaScript:newPopup('<yrcwww:link path='heatmap.do?token='/><bean:write name="proteinSetComparisonForm" property='clusteringToken' />&gradient=<bean:write name="gradient"/>');"><b>HTML</b></a></span>
				</logic:notPresent>
				</nobr>
		</logic:present>
		
	</td>
	<td align="right" valign="bottom">
		<a href="" onclick="openInformationPopup('<yrcwww:link path='pages/internal/docs/comparison.jsp'/>'); return false;">
   				<img src="<yrcwww:link path='images/info_16.png'/>" align="bottom" border="0"/></a>	
   		<span style="background-color:yellow; font-weight:bold; padding:3px;"><a href="#LEGEND">Legend</a></span>
	</td>
</tr>
</table>

</div>
</td>
</tr>
</table>
		
<!-- RESULTS TABLE -->
<div > 
<yrcwww:table name="comparison" tableId='compare_results' tableClass="table_basic sortable_table" center="true" />
</div>

<!-- PAGE RESULTS -->
<table id="compare_results_pager2">
<tr>
<td>
<%@include file="/pages/internal/pager_small.jsp" %>
</td>
</tr>
</table>



<!-- LEGEND -->
<a name="LEGEND"></a>
<%@include file="legend.jsp" %>

</yrcwww:contentbox>

</CENTER>

<%@ include file="/includes/footer.jsp" %>