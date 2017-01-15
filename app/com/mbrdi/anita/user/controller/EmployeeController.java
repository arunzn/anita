
package com.mbrdi.anita.user.controller;

import com.mbrdi.anita.basic.controller.Application;
import com.mbrdi.anita.basic.model.GridRequest;
import com.mbrdi.anita.basic.model.GridResponse;
import com.mbrdi.anita.basic.model.ValidationFailureException;
import com.mbrdi.anita.basic.security.Authenticator;
import com.mbrdi.anita.basic.security.RightsRequired;
import com.mbrdi.anita.basic.service.StorageService;
import com.mbrdi.anita.basic.util.ExcelUtil;
import com.mbrdi.anita.basic.util.Util;
import com.mbrdi.anita.message.model.DEVICE_TYPE;
import com.mbrdi.anita.message.model.NOTIFICATION_TYPE;
import com.mbrdi.anita.message.model.Notification;
import com.mbrdi.anita.message.service.MessageService;
import com.mbrdi.anita.message.service.NotificationService;
import com.mbrdi.anita.user.dao.UserDAO;
import com.mbrdi.anita.user.model.User;
import com.mbrdi.anita.user.service.UserService;
import org.apache.commons.io.FilenameUtils;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.Security;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

@Security.Authenticated(Authenticator.class)
public class EmployeeController extends Controller {

    @Inject
    FormFactory formFactory;

    public Result employees() {
        return ok(views.html.employee.employees.render());
    }

    public Result addEmployee() {
        Form<User> filledForm = formFactory.form(User.class).bindFromRequest();
        if (filledForm.hasErrors()) {
            return badRequest(views.html.employee.employee_new.render(filledForm.get(), filledForm));
        } else {
            User employee = filledForm.get();
            User old_user = null;
            try {
                old_user = UserService.addEmployee(employee);
                flash("MESSAGE", "Employee added successfully.");
            }catch (ValidationFailureException e) {
                flash("ERROR", e.getMessage());
                return badRequest(views.html.employee.employee_new.render(filledForm.get(), filledForm));
            }
            return redirect(com.mbrdi.anita.user.controller.routes.EmployeeController.employees());
        }
    }

    public Result editEmployeePage(String id) {
        User employee = UserDAO.get(id);
        return ok(views.html.employee.employee_edit.render(employee, formFactory.form(User.class)));
    }

    public Result editEmployee(String id) throws Exception {
        Form<User> filledForm = formFactory.form(User.class).bindFromRequest();
        if (filledForm.hasErrors()) {
            return badRequest(views.html.employee.employee_edit.render(filledForm.get(), formFactory.form(User.class)));
        } else {
            User employee_old = UserDAO.get(id);
            User employee = filledForm.get();

            employee._id = id;
            Util.smartCopy(employee, employee_old);
            try {
                UserDAO.save(employee_old);
            } catch (ValidationFailureException e) {
                e.printStackTrace();
                flash("ERROR", Messages.get(e.getMessage()));
                return badRequest(views.html.employee.employee_edit.render(filledForm.get(), formFactory.form(User.class)));
            }

            NotificationService.notifyUpdateProfile(employee_old);
            return redirect(com.mbrdi.anita.user.controller.routes.EmployeeController.employees());
        }
    }

    public Result addEmployeePage() {
        return ok(views.html.employee.employee_new.render(new User(), formFactory.form(User.class)));
    }


    public Result uploadEmployees() throws Exception {
        MultipartFormData body = request().body().asMultipartFormData();
        FilePart<File> file = body.getFile("file");

        if (file != null && "xlsx".equals(FilenameUtils.getExtension(file.getFilename()))) {
            List<User> users_fault = new ArrayList<User>();

            File employee_file = file.getFile();
            FileInputStream fileInputStream = null;

            byte[] bytes = new byte[(int) employee_file.length()];

            try {
                //convert file into array of bytes
                fileInputStream = new FileInputStream(employee_file);
                fileInputStream.read(bytes);
                fileInputStream.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            List<User> users = ExcelUtil.readXLS(bytes, User.class, new String[]{"emp_no", "name", "country_code", "phone", "email", "gender", "designation", "department"});
            for (User user : users) {
                if (!Util.isNullOrEmpty(user.email) && validEmail(user.email)) {

                    User old_user = UserDAO.getByEmail(user.email);
                    user.status = true;
                    if (old_user == null) {
                        try {
                            User saved_emp = UserDAO.save(user);
                            NotificationService.notifyUpdateProfile(saved_emp);
                            //TODO
                        } catch (Exception e) {
                            e.printStackTrace();
                            users_fault.add(user);
                        }
                    } else if(old_user.device_id == null){
                        user._id = old_user._id;
                        UserDAO.updateHard(user);
                    }
                }
            }

            if (users_fault.size() != 0) {
                String[] documentHeaders = {users_fault.size() + " employee are found duplicate entry as email or phone."};
                String[] dataColumnHeaders = {"name", "country Code", "Phone", "Email"};
                String[] fields = {"name", "country_code", "phone", "email"};

                byte[] bytes_new = ExcelUtil.writeXLS(users_fault, documentHeaders, dataColumnHeaders, fields, "Employee");

                response().setHeader("Content-disposition", "attachment; filename=employee.xlsx");

                return ok(bytes_new).as("application/vnd.ms-excel");
            }

            flash("MESSAGE", "All the employee are successfully uploaded.");
            return redirect(com.mbrdi.anita.user.controller.routes.EmployeeController.employees());

        } else {
            flash(Application.WARNING, "No xls file uploaded or work location");
            return redirect(com.mbrdi.anita.user.controller.routes.EmployeeController.employees());
        }
    }

    private boolean validEmail(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }


    /**
     * soft deletes this employee
     *
     * @param emp_id
     * @return
     */
    public Result deleteEmployee(String emp_id) {
        User user = UserDAO.get(emp_id);
        if (user.password == null && user.device_id == null) {
            UserDAO.deleteEmployee(user);
        } else {
            user.emp_no = null;
            User update_user = UserDAO.updateEmployee(user);
            NotificationService.notifyUpdateProfile(update_user);

            // delete dutyHour object on employee app
            // send notification
            if (user.phone_type != null && user.device_id != null && !"".equals(user.device_id)) {
                DEVICE_TYPE device_type = DEVICE_TYPE.get(user.phone_type);
                Notification notification = new Notification();
                notification.type = NOTIFICATION_TYPE.SHOW_ALERT_ONLY;
                // noti_msg.text = noti_msg.from_name
                // +" wants to track you. Approve/Reject:" + duty_hour;
                MessageService.sendNotification(user._id, user.device_id, device_type, notification);
            }

        }

        return ok("success");
    }


    /**
     * soft deletes this employee
     *
     * @param emp_id
     * @return
     */
    public Result makeAdmin(String emp_id) {

        if(session("user_id").equals(emp_id)){
            return ok("Can't modify self.");
        }
        User user = UserDAO.get(emp_id);
        user.isCorpAdmin = true;
        UserDAO.updateHard(user);

        return ok("success");
    }

    /**
     * soft deletes this employee
     *
     * @param emp_id
     * @return
     */
    public Result unmakeAdmin(String emp_id) {

        if(session("user_id").equals(emp_id)){
            return ok("Can't modify self.");
        }
        User user = UserDAO.get(emp_id);
        user.isCorpAdmin = null;
        UserDAO.updateHard(user);

        return ok("success");
    }


    public static String getProfilePicURL(String _id) {
        User user = UserDAO.get(_id);
        return StorageService.BUCKET_URL + user.country_code + "/CORP/EMPLOYEE/" + user._id + "/pic.jpeg";
    }


    public Result downloadEmployees() {

        List<User> users = UserDAO.getAll();

        String[] documentHeaders = {"All Employee Details"};
        String[] dataColumnHeaders = {"Name", "Emp No", "Phone", "email", "Designation", "Department"};
        String[] fields = {"name", "emp_no", "phone", "email","designation", "department"};

        byte[] bytes = ExcelUtil.writeXLS(users, documentHeaders, dataColumnHeaders, fields, "Employees");

        response().setHeader("Content-disposition", "attachment; filename=employees.xlsx");

        return ok(bytes).as("application/vnd.ms-excel");
    }

    public Result employeeGrid() {
        GridRequest gridRequest = Util.extractGridRequest(request());
        GridResponse<User> response = UserDAO.fetch(gridRequest);
        return ok(Json.toJson(response));
    }


}