/**
 * DeleteInstrumentAction.java
 * @author Vagisha Sharma
 * Oct 28, 2009
 * @version 1.0
 */
package org.yeastrc.www.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.yeastrc.ms.dao.DAOFactory;
import org.yeastrc.ms.dao.general.MsInstrumentDAO;
import org.yeastrc.ms.domain.general.impl.InstrumentBean;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

/**
 * 
 */
public class DeleteInstrumentAction extends Action{

    public ActionForward execute( ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response )
    throws Exception {

        // User making this request
        User user = UserUtils.getUser(request);
        if (user == null) {
            ActionErrors errors = new ActionErrors();
            errors.add("username", new ActionMessage("error.login.notloggedin"));
            saveErrors( request, errors );
            return mapping.findForward("authenticate");
        }

        // Restrict access to administrators
        Groups groupMan = Groups.getInstance();
        if (!groupMan.isMember(user.getResearcher().getID(), "administrators")) {
            ActionErrors errors = new ActionErrors();
            errors.add("access", new ActionMessage("error.access.invalidgroup"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }
        
        int instrumentId = 0;
        try {
            instrumentId = Integer.parseInt(request.getParameter("instrumentId"));
        }
        catch(NumberFormatException e){}
        
        if(instrumentId == 0) {
            ActionErrors errors = new ActionErrors();
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionMessage("error.general.invalid.id", "instrument: "+instrumentId));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }
        
        MsInstrumentDAO instrDao = DAOFactory.instance().getInstrumentDAO();
        if(instrDao.load(instrumentId) != null) {
            instrDao.deleteInstrument(instrumentId);
        }
        else {
            ActionErrors errors = new ActionErrors();
            errors.add(ActionErrors.GLOBAL_ERROR, 
                    new ActionMessage("error.general.invalid.id", "instrument: "+instrumentId+
                            ". No instrument found with this ID."));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }
        
        // Kick it to the view page
        return mapping.findForward("Success");

    }
}
