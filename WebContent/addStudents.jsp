<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
<%@include file="./WEB-INF/includes/includes.jsp" %>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Tigress</title>

</head>

<body>
<%@include file="./WEB-INF/includes/mainPane.jsp" %>
<table id="inner_content">
	<tr>
    	<td width="25%">
        <table class="inner_content_table">
        <tr>
        <td>
        <table class="news_table">
        <tr class="title_general">
        <td colspan="3" align="center">
        CSV Upload
        </td>
    	</tr>
        <tr>
        <td>
        <table class="news_item_table" width="100%">
        <form enctype="multipart/form-data" action="StudentCSVUpload" method="post">
        <tr>
        <td colspan="2" width="100%">
        <div align="center">
        <input type="file" name="csvFile"></input>
        </div>
        </td>
        </tr>
        <tr>
        <td colspan="2" width="100%">
        <div align="center">
        <input type="text" name="courseName" value="Course Name"></input>
        </div>
        </td>
        </tr>
        <tr colspan="2">
        <td width="100%">
        <div align="center">
        <input type="submit" value="Submit"></input>
        </div>
        </td>
        </tr>
        </form>
        </table>
        </td>
        </tr>
        </table>
        </td>
        </tr>
        </table>
        </td>
        <td width="50%">
        <table class="inner_content_table">
        <tr>
        <td>
        <form action="addStudentsSubmit.jsp" method="post">
        <table class="news_table">
        <tr class="title_general">
        <td colspan="3" align="center">
        Enter Information
        </td>
    	</tr>
        <tr>
        <td id="add_user_table">
        <table class="news_item_table" width="100%">
        <tr>
        <td width="33%">
        Student:
        </td>
        <td width="67%">
        0
        </td>
        </tr>
        <tr>
        <td colspan="2">
        &nbsp;
        </td>
        </tr>
        <tr>
        <td width="33%">
        Email Address:
        </td>
        <td width="67%">
        <input type="text" name="email_0" value="" style="width:90%"></input>
        </td>
        </tr>
        <tr>
        <td width="33%">
        First Name:
        </td>
        <td width="67%">
        <input type="text" name="fname_0" value="" style="width:90%"></input>
        </td>
        </tr>
        <tr>
        <td width="33%">
        Middle Name:
        </td>
        <td width="67%">
        <input type="text" name="mname_0" value="" style="width:90%"></input>
        </td>
        </tr>
        <tr>
        <td width="33%">
        Last Name:
        </td>
        <td width="67%">
        <input type="text" name="lname_0" value="" style="width:90%"></input>
        </td>
        </tr>
        <tr>
        <td width="33%">
        Password:
        </td>
        <td width="67%">
        <input type="text" name="password_0" value="Do not change for an autogenerated value." style="width:90%"></input>
        </td>
        </tr>
        <tr>
        <td width="33%">
        Course:
        </td>
        <td width="67%">
        <input type="text" name="course_0" value="" style="width:90%"></input>
        </td>
        </tr>
        </table>
        </td>
        </tr>
        <tr>
        <td>
        <table width="100%">
        <tr>
        <td width="33%">
        <div align="left">
        <input type="hidden" name="totalAdd" id="totalAdd" value="0"></input>
        <input type="button" value="Add Another" onclick="addAnother()"></input>
        </div>
        </td>
        <td width="67%">
        <div align=left>
        <input type="submit" value="Submit"></input>
        </div>
        </td>
        </tr>
        </table>
        </td>
        </tr>
        </table>
        </form>
        </td>
        </tr>
        </table>
        </td>
        <td width="25%">
        <table class="inner_content_table">
        <tr>
        <td>
        <%
        if(verbose)
        {
        	System.out.println("Got to hasUser conditional");
        }
		if(!hasUser)
		{
		%>
        	<table class="news_table" width="100%">
            <tr class="title_general">
            <td>
        	<div align="center">Login<br /></div>
            </td>
            </tr>
            </table>
            <table class="news_item_table" width="100%">
            <tr>
            <td>
        	<%@include file="./WEB-INF/includes/loginWindow.jsp" %>
            </td>
            </tr>
            </table>
        <%
		}
		else
		{
		%>
        	<table class="news_table" width="100%">
            <tr class="title_general">
            <td>
        	<div align="center">Logout<br /></div>
            </td>
            </tr>
            </table>
        	<table class="news_item_table" width="100%">
            <tr>
            <td>
        	<div align="center">Hi there, <%=displayName %>! Your last visit was <%
				java.util.Date logonDate=(java.util.Date)myUser.getAttribute("previousVisit");
				out.print(dateFormat.format(logonDate));
				%>.  Your role is <%
				out.print(myUser.getAttribute("role") + ".");
				if(myUser.getAttribute("role").equals("student"))
				{
					out.print("  Your administrator is " + myUser.getAttribute("administrator"));
				}
				%>.<br />Not you?<br /></div>
            <%@include file="./WEB-INF/includes/logoutWindow.jsp" %>
            </td>
            </tr>
            </table>
        <%
		}
		if(verbose)
        {
        	System.out.println("Got past hasUser conditional");
        }
		%>
        </td>
        </tr>
        </table>
        </td>
    </tr>
</table>
<%@include file="./WEB-INF/includes/footer.jsp" %>
</body>

</html>