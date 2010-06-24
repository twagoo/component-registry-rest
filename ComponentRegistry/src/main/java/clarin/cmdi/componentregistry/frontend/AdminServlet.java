package clarin.cmdi.componentregistry.frontend;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * 
 * @deprecated
 */
public class AdminServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    //    private Configuration configuration;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        //        configuration = Configuration.getInstance();
    }

    //    @Override
    //    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    //        resp.setContentType(MediaType.TEXT_HTML);
    //        if (!configuration.isAdminUser(req.getUserPrincipal())) {
    //            resp.getWriter().write("Unauthorised for administration tasks");
    //            resp.flushBuffer();
    //            return;
    //        }
    //        String parameter = req.getParameter("submit");
    //        if (parameter.equals("MigrateDates")) {
    //            MigrateData migrateData = new MigrateData();
    //            Principal userPrincipal = req.getUserPrincipal();
    //            migrateData.migrateDescriptions(ComponentRegistryFactory.getInstance().getPublicRegistry(), userPrincipal);
    //            migrateData
    //                    .migrateDescriptions(ComponentRegistryFactory.getInstance().getComponentRegistry(true, userPrincipal), userPrincipal);
    //            PrintWriter writer = resp.getWriter();
    //            if (migrateData.hasErrors()) {
    //                writer.write("Migrated dates finished some error occured (check server log).");
    //            } else {
    //                writer.write("Migrated all dates finished successfully.");
    //            }
    //            resp.flushBuffer();
    //        }
    //    }

}
