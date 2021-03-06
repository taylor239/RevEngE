<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
<%@include file="./WEB-INF/includes/includes.jsp" %>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>RevEngE</title>

</head>

<body>
<%@include file="./WEB-INF/includes/mainPane.jsp" %>
<table id="inner_content">
	<tr>
    	<td width="25%">
        <table class="inner_content_table">
        <%
        	ArrayList allStudents = myConnector.getAdminStudents((String)myUser.getAttribute("email"));
       		ArrayList courseNames = new ArrayList();
        	for(int x=0; x<allStudents.size(); x++)
	        {
	        	DBObj curObj = (DBObj)allStudents.get(x);
	        	String course = (String)curObj.getAttribute("course");
	        	if(course.equals(""))
	        	{
	        		continue;
	        	}
	        	
	        	if(!courseNames.contains(course))
	        	{
	        		courseNames.add(course);
	        	}
	        }
        %>
        <tr>
        <td>
        </td>
        </tr>
        </table>
        </td>
        <td width="50%">
        <table class="inner_content_table">
        <tr>
        <td>
        <table class="news_table">
        <tr class="title_general">
        <td align="center">
        Announcement
        </td>
    	</tr>
        <tr>
        <td>
        <table class="news_item_table" width="100%">
        <tr>
        <td colspan="2">
        You can make an announcement to your courses here, and RevEngE will send an email with your announcement to your students.  Select the course(s) you wish to make the announcement, type your announcement, and press send!
        </td>
        </tr>
        <tr>
        <td colspan="2">
        &nbsp;
        </td>
        </tr>
        <form action="announcementSubmit.jsp" method="post">
        <tr>
        <td colspan="2" width="100%">
        <input type="text" name="subject" value="Subject" onfocus="clearText(this);" style="width:100%;"></input>
        </td>
        </tr>
        <tr>
        <td colspan="2" width="100%">
        <div align="left">
        <textarea name="message" onfocus="clearText(this);" style="width:100%;">Message</textarea>
        <!--<input type="text" name="message" value="Message" onfocus="clearText(this);" style="width:100%;"></input> -->
        </div>
        </td>
        </tr>
        <%
        for(int y=0; y<courseNames.size(); y++)
        {
        	String courseName = (String)courseNames.get(y);
        %>
        	<tr colspan="2">
        	<td width="100%">
        	<table>
        	<tr>
        	<td>
        	<input type="checkbox" id="<%=courseName %>" name="<%=courseName %>" value="<%=courseName %>"></input>
	    	</td>
	    	<td>
	    	<b><%=courseName %></b>
	    	</td>
	    	</tr>
	    	</table>
	    	</td>
        	</tr>
        <%
        }
        %>
        <tr colspan="2">
        <td width="100%">
        <div align="left">
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
        <td width="25%">
        
        </td>
    </tr>
</table>
<%@include file="./WEB-INF/includes/footer.jsp" %>
</body>

</html>