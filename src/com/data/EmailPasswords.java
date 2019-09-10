package com.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class StudentCSVExport
 */
@WebServlet({ "/emailPasswords" })
public class EmailPasswords extends HttpServlet
{
	private static final long serialVersionUID = 1L;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EmailPasswords()
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		HttpSession session = request.getSession(true);
		TrafficAnalyzer accessor=TrafficAnalyzerPool.getAnalyzer();
		if(!accessor.allowImage(request.getRemoteAddr()+"image"))
		{
			return;
		}
		DatabaseConnector myConnector=(DatabaseConnector)session.getAttribute("connector");
		if(myConnector==null)
		{
			myConnector=new DatabaseConnector("pillar");
			session.setAttribute("connector", myConnector);
		}
		User myUser=(User)session.getAttribute("user");
		
		//ArrayList challengeAssignment = myConnector.getChallengeAssignment((String)myUser.getAttribute("email"), (String)request.getParameter("challengeName"));
		
		//String studentEmail = (String)request.getParameter("email");
		
		int numStudents = new Integer((String)request.getParameter("numStudents"));
		
		java.util.Date today = new java.util.Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyy_mm_dd_hh_mm_ss");
		String todayFormatted = dateFormat.format(today);
        
		response.setHeader("Content-Disposition", "attachment;filename=" + ("studentsAdded_" + todayFormatted + ".csv").replaceAll("\\s+",""));
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		ServletOutputStream out=response.getOutputStream();
		
		out.println("Email, First Name, Last Name, Course, Password");
		
		for(int x=0; x<numStudents; x++)
		{
			String curEmail = request.getParameter("studentEmail_" + x);
			String curFname = request.getParameter("studentFname_" + x);
			String curLname = request.getParameter("studentLname_" + x);
			String curCourse = request.getParameter("course_" + x);
			String curPassword = request.getParameter("studentPassword_" + x);
			EmailSender mySender = EmailSender.getEmailSender("revenge@cs.arizona.edu");
			mySender.sendEmail(curEmail, "Your RevEngE Account", "Hello " + curFname + " " + curLname + ". You have been signed up for an account on revenge.cs.arizona.edu/RevEngE/. Your username is your email address, and your password is " + curPassword);
			//out.println(curEmail + ", " + curFname + ", " + curLname + ", " + curCourse + ", " + curPassword);
			out.println("<meta http-equiv=\"Refresh\" content=\"0; url=index.jsp?alert_message=Emails sent.\" />");
		}
		
		out.close();
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		// TODO Auto-generated method stub
		doGet(request, response);
	}
}
