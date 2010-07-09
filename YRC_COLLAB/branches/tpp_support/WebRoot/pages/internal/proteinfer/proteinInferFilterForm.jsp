
<%@page import="org.yeastrc.ms.domain.protinfer.ProteinUserValidation"%>
<%@page import="org.yeastrc.bio.go.GOUtils"%>
<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<script type="text/javascript">
$(document).ready(function() {
	$("input[name='validationStatus'][value='All']").click(function() {
		$("input[name='validationStatus'][value!='All']").each(function() {
			this.checked = false;
		});
	});
	$("input[name='validationStatus'][value!='All']").click(function() {
		$("input[name='validationStatus'][value='All']").each(function() {
			this.checked = false;
		});
	});
	
	$("input[name='chargeStates'][value='All']").click(function() {
		$("input[name='chargeStates'][value!='All']").each(function() {
			this.checked = false;
		});
	});
	$("input[name='chargeStates'][value!='All']").click(function() {
		$("input[name='chargeStates'][value='All']").each(function() {
			this.checked = false;
		});
	});
});

function openGOTermSearcher() {
	var url = "<yrcwww:link path='goTermSearch.do'/>";
	// we want the result to open in a new window
	window.open(url, 'gotermsearcher', 'scrollbars=yes,menubar=no,height=500,width=650,resizable=yes,toolbar=no,status=no');
}

// terms is an array of goTerms
function addToGoSearchTerms(terms) {
	for(var i = 0; i < terms.length; i++) {
		addToGoTermFilters(terms[i], false);
	}
}
	
function addToGoTermFilters(goTerm, warn) {
	var current = $("form#filterForm input[name='goTerms']").val();
	// If this terms in not already in the list add it.
	if(current.indexOf(goTerm) == -1) {
		var terms = current;
		if(current)
			terms = terms+","
		terms = terms+goTerm;
		$("form#filterForm input[name='goTerms']").val(terms);
	}
	else if(warn) {
		alert(goTerm+" has already been added");
	}
	$(".go_filter_add[id='"+goTerm+"']").hide();
	$(".go_filter_remove[id='"+goTerm+"']").show();
}

function removeFromGoTermFilters(goTerm, warn) {
	var current = $("form#filterForm input[name='goTerms']").val();
	// If this terms is in the list remove it
	var idx = current.indexOf(goTerm);
	if(idx != -1) {
		// get everything before the goTerm
		var term = current.substring(0,idx);
		if(term.charAt(term.length - 1) == ',') {
			term = term.substring(0,term.length-1);
		}
		// get everything after the goTerm
		term = term+current.substring(idx+goTerm.length);
		//alert(term);
		if(term.charAt(term.length - 1) == ',') {
			term = term.substring(0,term.length-1);
		}
		
		$("form#filterForm input[name='goTerms']").val(term);
	}
	else if(warn) {
		alert(goTerm+" was not found in the filter list");
	}
	$(".go_filter_add[id='"+goTerm+"']").show();
	$(".go_filter_remove[id='"+goTerm+"']").hide();
}
</script>

  <html:form action="/proteinInferGateway" method="post" styleId="filterForm" >
  
  <html:hidden name="proteinInferFilterForm" property="pinferId" />
  <html:hidden name="proteinInferFilterForm" property="doDownload" />
  <html:hidden name="proteinInferFilterForm" property="doGoSlimAnalysis" />
  <html:hidden name="proteinInferFilterForm" property="getGoSlimTree" />
  <html:hidden name="proteinInferFilterForm" property="doGoEnrichAnalysis" />
  <html:hidden name="proteinInferFilterForm" property="goAspect" />
  <html:hidden name="proteinInferFilterForm" property="goSlimTermId" />
  <html:hidden name="proteinInferFilterForm" property="goEnrichmentPVal" />
  <html:hidden name="proteinInferFilterForm" property="speciesId" />
  
  
  <TABLE CELLPADDING="5px" CELLSPACING="5px" align="center" style="border: 1px solid gray;">
  
  <!-- Filtering options -->
  <tr>
  
  <td><table>
  <tr>
  <td>Peptides: </td>
  <td>
  	Min <html:text name="proteinInferFilterForm" property="minPeptides" size="3"></html:text>
  	Max <html:text name="proteinInferFilterForm" property="maxPeptides" size="3"></html:text>
  </td>
  </tr>
  <tr>
  <td>Unique Peptides: </td>
  <td>
  	Min <html:text name="proteinInferFilterForm" property="minUniquePeptides" size="3"></html:text>
  	Max <html:text name="proteinInferFilterForm" property="maxUniquePeptides" size="3"></html:text>
  </td>
  </tr>
  <tr>
  <td>Protein Mol. Wt.: </td>
  <td>
  	Min <html:text name="proteinInferFilterForm" property="minMolecularWt" size="3"></html:text>
  	Max <html:text name="proteinInferFilterForm" property="maxMolecularWt" size="3"></html:text>
  </td>
  </tr>
  </table></td>
  
  <td><table>
  <tr>
  <td>Coverage(%):</td>
  <td>
  	Min <html:text name="proteinInferFilterForm" property="minCoverage" size="3"></html:text>
  	Max <html:text name="proteinInferFilterForm" property="maxCoverage" size="3"></html:text>
  </td>
  </tr>
  <tr>
  <td>Spectrum Matches: </td>
  <td>
  	Min <html:text name="proteinInferFilterForm" property="minSpectrumMatches" size="3"></html:text>
  	Max <html:text name="proteinInferFilterForm" property="maxSpectrumMatches" size="3"></html:text>
  </td>
  </tr>
  <tr>
  <td>Protein pI: </td>
  <td>
  	Min <html:text name="proteinInferFilterForm" property="minPi" size="3"></html:text>
  	Max <html:text name="proteinInferFilterForm" property="maxPi" size="3"></html:text>
  </td>
  </tr>
  </table></td>
  
  <td valign="top"><table>
  
  <logic:notPresent name="goView">
  <tr>
  	<td colspan="2">Group Indistinguishable Proteins: </td>
  	<td>
  		<html:radio name="proteinInferFilterForm" property="joinGroupProteins" value="true">Yes</html:radio>
  	</td>
  	<td>
  		<html:radio name="proteinInferFilterForm" property="joinGroupProteins" value="false">No</html:radio>
  	</td>
  </tr>
  </logic:notPresent>
  
  <tr>
  	<td colspan="2">Show Proteins: </td>
  	<td>
  		<html:radio name="proteinInferFilterForm" property="showAllProteins" value="true">All</html:radio>
  	</td>
  	<td>
  		<html:radio name="proteinInferFilterForm" property="showAllProteins" value="false">Parsimonious</html:radio>
  	</td>
  </tr>
  </table></td>
  </tr>
  
  <tr>
  	<td colspan="2">
  		Validation Status: 
  		<html:multibox name="proteinInferFilterForm" property="validationStatus" value="All"/> All
  		<html:multibox name="proteinInferFilterForm" property="validationStatus" 
  					   value="<%=String.valueOf(ProteinUserValidation.UNVALIDATED.getStatusChar()) %>"/> Unvalidated 
  		<html:multibox name="proteinInferFilterForm" property="validationStatus"  
  		               value="<%=String.valueOf(ProteinUserValidation.ACCEPTED.getStatusChar()) %>"/> Accepted
  		<html:multibox name="proteinInferFilterForm" property="validationStatus"  
  		               value="<%=String.valueOf(ProteinUserValidation.REJECTED.getStatusChar()) %>"/> Rejected
  		<html:multibox name="proteinInferFilterForm" property="validationStatus"  
  		               value="<%=String.valueOf(ProteinUserValidation.NOT_SURE.getStatusChar()) %>"/> Not Sure
  	</td>
  	<td>
  		Exclude Indistinguishable Groups: <html:checkbox name="proteinInferFilterForm" property="excludeIndistinProteinGroups" value="true"/>
  	</td>
  </tr>
  
  <tr>
  	<td colspan="2">
  		Include Charge: &nbsp;&nbsp;
  		<html:multibox name="proteinInferFilterForm" property="chargeStates" value="All"/> All &nbsp;
  		<html:multibox name="proteinInferFilterForm" property="chargeStates" value="1"/> +1 &nbsp;
  		<html:multibox name="proteinInferFilterForm" property="chargeStates" value="2"/> +2 &nbsp;
  		<html:multibox name="proteinInferFilterForm" property="chargeStates" value="3"/> +3 &nbsp;
  		<html:multibox name="proteinInferFilterForm" property="chargeStates" value="4"/> +4 &nbsp;
  		<html:multibox name="proteinInferFilterForm" property="chargeStates" value=">4"/> &gt; +4   
  	</td>
  </tr>
  
  
  <tr>
  	<td colspan="3">
  	<table align="left">
  		<logic:present name="goSupported">
  		<tr>
  			<td valign="top">GO Term(s): <br/><span class="clickable underline" style="color:red; font-weight:bold;" 
  			onclick="javascript:openGOTermSearcher();return false;">Search</span></td>
  			<td valign="top"><html:text name="proteinInferFilterForm" property="goTerms" size="40"></html:text><br>
  				<span style="font-size:8pt;">Enter a comma-separated list of GO terms (e.g. GO:0006950)</span>
  			</td>
  			<td valign="top" colspan="2">
  				<html:checkbox name="proteinInferFilterForm" property="matchAllGoTerms" title="Return proteins that match all terms">Match All </html:checkbox>
  				<html:checkbox name="proteinInferFilterForm" property="exactGoAnnotation" title="Return proteins directly annotated with the GO terms">Exact </html:checkbox>
  				&nbsp;
  				<nobr>
  				Exclude: 
  				<html:checkbox name="proteinInferFilterForm" property="excludeIea"><span title="Inferred from Electronic Annotation">IEA</span></html:checkbox>
  				<html:checkbox name="proteinInferFilterForm" property="excludeNd"><span title="No Biological Data available">ND</span></html:checkbox>
  				<html:checkbox name="proteinInferFilterForm" property="excludeCompAnalCodes"><span title="Computational Analysis Evidence Codes">ISS, ISO, ISA, ISM, IGC, RCA</span></html:checkbox>
  				</nobr>
  			</td>
  		</tr>
  		</logic:present>
  		
  		<tr>
  			<td valign="top">Fasta ID(s): </td>
  			<td valign="top"><html:text name="proteinInferFilterForm" property="accessionLike" size="40"></html:text><br>
  				<span style="font-size:8pt;">Enter a comma-separated list of complete or partial identifiers</span>
  			</td>
  			<td valign="top">Peptide: </td>
  			<td valign="top">
  				<html:text name="proteinInferFilterForm" property="peptide" size="40"></html:text>
  				<span style="font-size:8pt;">Exact Match:<html:checkbox property="proteinInferFilterForm" property="exactPeptideMatch"></html:checkbox></span>
  			</td>
  			
  		</tr>
  		<tr>
  			<td valign="top">Description Include: </td>
  			<td valign="top"><html:text name="proteinInferFilterForm" property="descriptionLike" size="40"></html:text>
  			</td>
  			<td valign="top">Exclude: </td>
  			<td valign="top">
  				<html:text name="proteinInferFilterForm" property="descriptionNotLike" size="40"></html:text>
  				<span style="font-size:8pt;">Search All:<html:checkbox property="proteinInferFilterForm" property="searchAllDescriptions"></html:checkbox></span>
  			</td>
  		</tr>
  		<tr>
  		<td></td>
  		<td colspan="3" ">
  			<div style="font-size:8pt;" align="left">Enter a comma-separated list of terms.
  			Descriptions will be included from the fasta file(s) associated with the experiment(s) <br>for
  			this protein inference as well as species specific databases (e.g. SGD) 
  			if a target species is associated with the experiment(s).
  			<br>Check "Search All" to include descriptions from Swiss-Prot and NCBI-NR. 
  			<br/><font color="red">NOTE: Description searches can be time consuming, especially when "Search All" is checked.</font></div>
  		</td>
  		</tr>
  	</table>
  	</td>
  </tr>
  
  <tr>
    	<td colspan="3" align="center">
    		<button class="plain_button" style="margin-top:2px;" 
    		        onclick="javascript:updateResults();return false;">Update</button>
    		<!--<html:submit styleClass="plain_button" style="margin-top:2px;">Update</html:submit>-->
    	</td>
    	 
  </tr>
  
 </TABLE>
 

<div align="center" style="margin:10 0 5 0;">
  	<a href="" onclick="javascript:downloadResults();return false;" ><b>Download Results</b></a> &nbsp; 
  	<html:checkbox name="proteinInferFilterForm"property="printPeptides" >Include Peptides</html:checkbox>
  	<html:checkbox name="proteinInferFilterForm"property="printDescriptions" >Include Descriptions</html:checkbox>
  	<html:checkbox name="proteinInferFilterForm"property="collapseGroups" >Collapse Protein Groups</html:checkbox>
 </div>


</html:form>
