package edu.uwpr.protinfer.database.dao.idpicker.ibatis;

import junit.framework.TestCase;
import edu.uwpr.protinfer.database.dao.ProteinferDAOFactory;
import edu.uwpr.protinfer.database.dto.ProteinInferenceProgram;
import edu.uwpr.protinfer.database.dto.ProteinferStatus;
import edu.uwpr.protinfer.database.dto.idpicker.IdPickerRun;

public class IdPickerRunDAOTest extends TestCase {

    private static final ProteinferDAOFactory factory = ProteinferDAOFactory.testInstance();
    private static final IdPickerRunDAO runDao = factory.getIdPickerRunDao(); 
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public final void testSaveIdPickerRunSummary() {
        IdPickerRun run = new IdPickerRun();
        run.setComments("some comments");
        run.setStatus(ProteinferStatus.RUNNING);
        run.setProgram(ProteinInferenceProgram.IDPICKER);
        run.setNumUnfilteredProteins(2005);
        run.setNumUnfilteredPeptides(3110);
        
        int id = runDao.save(run);
        assertEquals(1, id);
        run.setId(id);
        runDao.saveIdPickerRunSummary(run);
    }
    
    public final void testGetProteinferRun() {
        IdPickerRun run = runDao.getProteinferRun(1);
        assertEquals(2005, run.getNumUnfilteredProteins());
        assertEquals(3110, run.getNumUnfilteredPeptides());
        assertEquals("some comments", run.getComments());
        assertEquals(ProteinferStatus.RUNNING, run.getStatus());
        assertEquals(ProteinInferenceProgram.IDPICKER, run.getProgram());
    }
}