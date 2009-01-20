/**
 * SortProteinferResultsAction.java
 * @author Vagisha Sharma
 * Jan 9, 2009
 * @version 1.0
 */
package org.yeastrc.www.proteinfer;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.yeastrc.www.proteinfer.idpicker.WIdPickerProteinGroup;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import edu.uwpr.protinfer.PeptideDefinition;
import edu.uwpr.protinfer.database.dto.ProteinFilterCriteria;
import edu.uwpr.protinfer.database.dto.ProteinFilterCriteria.SORT_BY;
import edu.uwpr.protinfer.util.TimeUtils;

/**
 * 
 */
public class SortProteinferResultsAjaxAction extends Action{

    private static final Logger log = Logger.getLogger(SortProteinferResultsAjaxAction.class);
    
    public ActionForward execute( ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response )
    throws Exception {
        
        // User making this request
        User user = UserUtils.getUser(request);
        if (user == null) {
            response.getWriter().write("You are not logged in!");
            response.setStatus(HttpServletResponse.SC_SEE_OTHER); // Status code (303) indicating that the response to the request can be found under a different URI.
            return null;
        }
        
        // get the protein inference ID from the request
        int pinferId = 0;
        try {pinferId = Integer.parseInt(request.getParameter("inferId"));}
        catch(NumberFormatException e){};
        if(pinferId <= 0) {
            log.error("Invalid protein inference run id: "+pinferId);
            response.setContentType("text/html");
            response.getWriter().write("ERROR: Invalid protein inference ID: "+pinferId);
            return null;
        }
        
        // make sure protein inference ID in the request matches the ID for results stored in the session
        Integer pinferId_session = (Integer)request.getSession().getAttribute("pinferId");
        if(pinferId_session == null || pinferId_session != pinferId) {
            // redirect to the /viewProteinInferenceResult action if this different from the
            // protein inference ID stored in the session
            ActionForward newResults = mapping.findForward( "ViewNewResults" ) ;
            newResults = new ActionForward( newResults.getPath() + "inferId="+pinferId, newResults.getRedirect() ) ;
            return newResults;
        }
        
        // Protein filter criteria from the session
        ProteinFilterCriteria filterCriteria = (ProteinFilterCriteria) request.getSession().getAttribute("pinferFilterCriteria");
        PeptideDefinition peptideDef = filterCriteria.getPeptideDefinition();
        
        
        // How are we displaying the results (grouped by protein group or individually)
        boolean group = filterCriteria.isGroupProteins();
        request.setAttribute("groupProteins", group);
        
        
        // Get the list of filtered and sorted protein IDs stored in the session
        List<Integer> storedProteinIds = (List<Integer>) request.getSession().getAttribute("proteinIds");
        if(storedProteinIds == null || storedProteinIds.size() == 0) {
            // redirect to the /viewProteinInferenceResult action if no proteinIds are stored in the session
            ActionForward newResults = mapping.findForward( "ViewNewResults" ) ;
            newResults = new ActionForward( newResults.getPath() + "inferId="+pinferId, newResults.getRedirect() ) ;
            return newResults;
        }
        
       
        
        SORT_BY sortBy_session = filterCriteria.getSortBy();
        String sortBy_request = (String) request.getParameter("sortBy");
        
        long s = System.currentTimeMillis();
        log.info("Sorting results for protein inference: "+pinferId+"; sort by: "+sortBy_request);
        
        if(sortBy_request != null){
            SORT_BY sortBy_r = SORT_BY.getSortByForString(sortBy_request);
            if(sortBy_r != sortBy_session) {
                log.info("Sorting results by "+sortBy_r.name());
                // resort  the results based on the given criteria
                List<Integer> newOrderIds = IdPickerResultsLoader.getSortedProteinIds(pinferId, 
                            peptideDef, 
                            storedProteinIds, 
                            sortBy_r, 
                            group);
                
                filterCriteria.setSortBy(sortBy_r);
                request.getSession().setAttribute("pinferFilterCriteria", filterCriteria);
                request.getSession().setAttribute("proteinIds", newOrderIds);
                storedProteinIds = newOrderIds;
            }
        }
        
        // page number is now 1
        int pageNum = 1;
        
        
        // determine the list of proteins we will be displaying
        ProteinferResultsPager pager = ProteinferResultsPager.instance();
        List<Integer> proteinIds = pager.page(storedProteinIds, pageNum, true);
        
        // get the protein groups
        List<WIdPickerProteinGroup> proteinGroups = IdPickerResultsLoader.getProteinGroups(pinferId, proteinIds, group, peptideDef);
        request.setAttribute("proteinGroups", proteinGroups);
        
        
        // get the list of page numbers to display
        int pageCount = ProteinferResultsPager.instance().getPageCount(storedProteinIds.size());
        List<Integer> pages = ProteinferResultsPager.instance().getPageList(storedProteinIds.size(), pageNum);
        
        request.setAttribute("currentPage", pageNum);
        request.setAttribute("onFirst", pageNum == 1);
        request.setAttribute("onLast", (pageNum == pages.get(pages.size() - 1)));
        request.setAttribute("pages", pages);
        request.setAttribute("pageCount", pageCount);
        
        long e = System.currentTimeMillis();
        log.info("Total time (SortProteinInferenceResultAjaxAction): "+TimeUtils.timeElapsedSeconds(s, e));
        
        return mapping.findForward("Success");
        
    }
}