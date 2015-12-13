/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") +  you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openmeetings.axis.services;

import static org.apache.openmeetings.util.OpenmeetingsVariables.webAppRootKey;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.axis2.AxisFault;
import org.apache.openmeetings.data.calendar.management.AppointmentLogic;
import org.apache.openmeetings.db.dao.calendar.AppointmentCategoryDao;
import org.apache.openmeetings.db.dao.calendar.AppointmentDao;
import org.apache.openmeetings.db.dao.calendar.AppointmentReminderTypDao;
import org.apache.openmeetings.db.dao.room.RoomTypeDao;
import org.apache.openmeetings.db.dao.server.SessiondataDao;
import org.apache.openmeetings.db.dao.user.UserDao;
import org.apache.openmeetings.db.dto.calendar.AppointmentDTO;
import org.apache.openmeetings.db.dto.calendar.AppointmentReminderTypeDTO;
import org.apache.openmeetings.db.entity.calendar.Appointment;
import org.apache.openmeetings.db.entity.calendar.AppointmentCategory;
import org.apache.openmeetings.db.entity.calendar.AppointmentReminderTyps;
import org.apache.openmeetings.db.entity.calendar.MeetingMember;
import org.apache.openmeetings.db.entity.user.User;
import org.apache.openmeetings.db.entity.user.User.Right;
import org.apache.openmeetings.util.AuthLevelUtil;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * CalendarService contains methods to create, edit delete calendar meetings
 * 
 * @author sebawagner
 * @webservice CalendarService
 * 
 */
public class CalendarWebService {
	private static final Logger log = Red5LoggerFactory.getLogger(CalendarWebService.class, webAppRootKey);

	@Autowired
	private AppointmentDao appointmentDao;
	@Autowired
	private AppointmentLogic appointmentLogic;
	@Autowired
	private SessiondataDao sessiondataDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private AppointmentCategoryDao appointmentCategoryDao;
	@Autowired
	private AppointmentReminderTypDao appointmentReminderTypDao;
	@Autowired
	private RoomTypeDao roomTypeDao;

	private List<AppointmentDTO> getAppointments(List<Appointment> list) {
		List<AppointmentDTO> result = new ArrayList<>(list.size());
		for (Appointment a : list) {
			result.add(new AppointmentDTO(a));
		}
		return result;
	}
	
	/**
	 * Load appointments by a start / end range for the current SID
	 * 
	 * @param SID
	 *            The SID of the User. This SID must be marked as Loggedin
	 * @param starttime
	 *            start time, yyyy-mm-dd
	 * @param endtime
	 *            end time, yyyy-mm-dd
	 *            
	 * @return - list of appointments in range
	 */
	public List<AppointmentDTO> getAppointmentByRange(String SID, Calendar starttime, Calendar endtime) {
		log.debug("getAppointmentByRange : startdate - " + starttime.getTime() + ", enddate - " + endtime.getTime());
		try {
			Long users_id = sessiondataDao.checkSession(SID);
			if (AuthLevelUtil.hasUserLevel(userDao.getRights(users_id))) {
				return getAppointments(appointmentDao.getAppointmentsByRange(users_id, starttime.getTime(), endtime.getTime()));
			}
		} catch (Exception err) {
			log.error("[getAppointmentByRange]", err);
		}
		return null;
	}

	/**
	 * Load appointments by a start / end range for the userId
	 * 
	 * @param SID
	 *            The SID of the User. This SID must be marked as Loggedin
	 * @param userId
	 *            the userId the calendar events should be loaded
	 * @param starttime
	 *            start time, yyyy-mm-dd
	 * @param endtime
	 *            end time, yyyy-mm-dd
	 *            
	 * @return - list of appointments in range
	 */
	public List<AppointmentDTO> getAppointmentByRangeForUserId(String SID, long userId, Calendar starttime, Calendar endtime) {
		log.debug("getAppointmentByRangeForUserId : startdate - " + starttime.getTime() + ", enddate - " + endtime.getTime());
		try {
			Long users_id = sessiondataDao.checkSession(SID);
			if (AuthLevelUtil.hasWebServiceLevel(userDao.getRights(users_id))) {
				return getAppointments(appointmentDao.getAppointmentsByRange(userId, starttime.getTime(), endtime.getTime()));
			}
		} catch (Exception err) {
			log.error("[getAppointmentByRangeForUserId]", err);
		}
		return null;
	}

	/**
	 * Get the next Calendar event for the current user of the SID
	 * 
	 * @param SID
	 *            The SID of the User. This SID must be marked as Loggedin
	 * @return - next Calendar event
	 */
	public AppointmentDTO getNextAppointment(String SID) {
		try {
			Long users_id = sessiondataDao.checkSession(SID);
			if (AuthLevelUtil.hasUserLevel(userDao.getRights(users_id))) {
				return new AppointmentDTO(appointmentDao.getNextAppointment(users_id, new Date()));
			}
		} catch (Exception err) {
			log.error("[getNextAppointmentById]", err);
		}
		return null;

	}

	/**
	 * Get the next Calendar event for userId
	 * 
	 * @param SID
	 *            The SID of the User. This SID must be marked as Loggedin
	 *            
	 * @return - next Calendar event
	 */
	public AppointmentDTO getNextAppointmentForUserId(String SID, long userId) {
		try {
			Long users_id = sessiondataDao.checkSession(SID);
			if (AuthLevelUtil.hasWebServiceLevel(userDao.getRights(users_id))) {
				return new AppointmentDTO(appointmentDao.getNextAppointment(userId, new Date()));
			}
		} catch (Exception err) {
			log.error("[getNextAppointmentById]", err);
		}
		return null;

	}

	/**
	 * Search a calendar event for the current SID
	 * 
	 * @param SID
	 *            The SID of the User. This SID must be marked as Loggedin
	 * @param appointmentName
	 *            the search string
	 *            
	 * @return - calendar event list
	 */
	public List<AppointmentDTO> searchAppointmentByTitle(String SID, String appointmentName) {
		try {
			Long users_id = sessiondataDao.checkSession(SID);
			if (AuthLevelUtil.hasUserLevel(userDao.getRights(users_id))) {
				return getAppointments(appointmentDao.searchAppointmentsByTitle(users_id, appointmentName));
			}
		} catch (Exception err) {
			log.error("[searchAppointmentByName]", err);
		}
		return null;

	}

	/**
	 * Save an appointment
	 * 
	 * @param SID
	 *            The SID of the User. This SID must be marked as Loggedin
	 * @param appointmentName
	 *            name of the calendar event
	 * @param appointmentLocation
	 *            location info text of the calendar event
	 * @param appointmentDescription
	 *            description test of the calendar event
	 * @param appointmentstart
	 *            start as Date yyyy-mm-ddThh:mm:ss
	 * @param appointmentend
	 *            end as Date yyyy-mm-ddThh:mm:ss
	 * @param isDaily
	 *            if the calendar event should be repeated daily (not
	 *            implemented)
	 * @param isWeekly
	 *            if the calendar event should be repeated weekly (not
	 *            implemented)
	 * @param isMonthly
	 *            if the calendar event should be repeated monthly (not
	 *            implemented)
	 * @param isYearly
	 *            if the calendar event should be repeated yearly (not
	 *            implemented)
	 * @param categoryId
	 *            the category id of the calendar event
	 * @param remind
	 *            the reminder type of the calendar event
	 * @param mmClient
	 *            List of clients, comma separated string, <br/>
	 *            sample: '1,firstname,lastname,hans.tier@gmail.com,1,Etc/GMT+1'
	 *            to add multiple clients you can use the same GET parameter in
	 *            the URL multiple times, for example:
	 *            &amp;mmClient='1,firstname,lastname,hans.tier@gmail.com,1,Etc/GMT+1'&amp;mmClient='2,firstname,lastname,hans.tier@gmail.com,1,Etc/GMT+1'
	 *             (Please NOTE mmClient value is enclosed in single quotes)
	 * @param roomType
	 *            the room type for the calendar event
	 * @param languageId
	 *            the language id of the calendar event, notification emails
	 *            will be send in this language
	 * @param isPasswordProtected
	 *            if the room is password protected
	 * @param password
	 *            the password for the room
	 *            
	 * @return - id of appointment saved
	 */
	public Long saveAppointment(String SID, String appointmentName,
			String appointmentLocation, String appointmentDescription,
			Calendar appointmentstart, Calendar appointmentend,
			Boolean isDaily, Boolean isWeekly, Boolean isMonthly,
			Boolean isYearly, Long categoryId, Long remind, String[] mmClient,
			Long roomType, Long languageId,
			Boolean isPasswordProtected, String password, long roomId) {
		//Seems to be create
		log.debug("saveAppointMent SID:" + SID);

		try {
			Long users_id = sessiondataDao.checkSession(SID);
			log.debug("saveAppointMent users_id:" + users_id);

			if (AuthLevelUtil.hasUserLevel(userDao.getRights(users_id))) {
				Appointment a = appointmentLogic.getAppointment(appointmentName, appointmentLocation, appointmentDescription,
						appointmentstart, appointmentend, isDaily, isWeekly, isMonthly, isYearly, categoryId, remind,
						mmClient, roomType, languageId, isPasswordProtected, password, roomId, users_id);
				return appointmentDao.update(a, users_id).getId();
			} else {
				log.error("saveAppointment : wrong user level");
			}
		} catch (Exception err) {
			log.error("[saveAppointment]", err);
		}
		return null;

	}

	/**
	 * Update an calendar event time only
	 * 
	 * @param SID
	 *            The SID of the User. This SID must be marked as Loggedin
	 * @param appointmentId
	 *            the calendar event that should be updated
	 * @param appointmentstart
	 *            start yyyy-mm-dd
	 * @param appointmentend
	 *            end yyyy-mm-dd
	 * @param languageId
	 *            the language id
	 *            
	 * @return - id of appointment updated
	 */
	public Long updateAppointmentTimeOnly(String SID, Long appointmentId, Date appointmentstart, Date appointmentend, Long languageId) {
		try {
			Long users_id = sessiondataDao.checkSession(SID);
			Set<Right> rights = userDao.getRights(users_id);
			if (AuthLevelUtil.hasUserLevel(rights)) {
				Appointment a = appointmentDao.get(appointmentId);
				if (!AuthLevelUtil.hasAdminLevel(rights) && !a.getOwner().getUser_id().equals(users_id)) {
					throw new AxisFault("The Appointment cannot be updated by the given user");
				}
				if (!a.getStart().equals(appointmentstart) || !a.getEnd().equals(appointmentend)) {
					a.setStart(appointmentstart);
					a.setEnd(appointmentend);
					//FIXME this might change the owner!!!!!
					return appointmentDao.update(a, users_id).getId();
				}					
			}
		} catch (Exception err) {
			log.error("[updateAppointment]", err);
		}
		return null;

	}

	/**
	 * Save an appointment
	 * 
	 * @param SID
	 *            The SID of the User. This SID must be marked as Loggedin
	 * @param appointmentId
	 *            the id to update
	 * @param appointmentName
	 *            name of the calendar event
	 * @param appointmentLocation
	 *            location info text of the calendar event
	 * @param appointmentDescription
	 *            description test of the calendar event
	 * @param appointmentstart
	 *            start as Date yyyy-mm-ddThh:mm:ss
	 * @param appointmentend
	 *            end as Date yyyy-mm-ddThh:mm:ss
	 * @param isDaily
	 *            if the calendar event should be repeated daily (not
	 *            implemented)
	 * @param isWeekly
	 *            if the calendar event should be repeated weekly (not
	 *            implemented)
	 * @param isMonthly
	 *            if the calendar event should be repeated monthly (not
	 *            implemented)
	 * @param isYearly
	 *            if the calendar event should be repeated yearly (not
	 *            implemented)
	 * @param categoryId
	 *            the category id of the calendar event
	 * @param remind
	 *            the reminder type of the calendar event
	 * @param mmClient
	 *            List of clients, comma separated string, <br/>
	 *            sample: '1,firstname,lastname,hans.tier@gmail.com,1,Etc/GMT+1'
	 *            to add multiple clients you can use the same GET parameter in
	 *            the URL multiple times, for example:
	 *            &amp;mmClient='1,firstname,lastname,hans.tier@gmail.com,1,Etc/GMT+1'&amp;mmClient='2,firstname,lastname,hans.tier@gmail.com,1,Etc/GMT+1'
	 *             (Please NOTE mmClient value is enclosed in single quotes)
	 * @param roomType
	 *            the room type for the calendar event
	 * @param languageId
	 *            the language id of the calendar event, notification emails
	 *            will be send in this language
	 * @param isPasswordProtected
	 *            if the room is password protected
	 * @param password
	 *            the password for the room
	 *            
	 * @return - id of appointment updated
	 */
	public Long updateAppointment(String SID, Long appointmentId,
			String appointmentName, String appointmentLocation,
			String appointmentDescription, Calendar appointmentstart,
			Calendar appointmentend, Boolean isDaily, Boolean isWeekly,
			Boolean isMonthly, Boolean isYearly, Long categoryId, Long remind,
			String[] mmClient, Long roomType, Long languageId,
			Boolean isPasswordProtected, String password) throws AxisFault {
		try {

			Long users_id = sessiondataDao.checkSession(SID);
			Set<Right> rights = userDao.getRights(users_id);

			if (AuthLevelUtil.hasWebServiceLevel(rights) || AuthLevelUtil.hasAdminLevel(rights)) {
				// fine
			} else if (AuthLevelUtil.hasUserLevel(rights)) {
				// check if the appointment belongs to the current user
				Appointment a = appointmentDao.get(appointmentId);
				if (!a.getOwner().getUser_id().equals(users_id)) {
					throw new AxisFault("The Appointment cannot be updated by the given user");
				}
			} else {
				throw new AxisFault("Not allowed to preform that action, Authenticate the SID first");
			}

			Appointment a = appointmentDao.get(appointmentId);
			a.setTitle(appointmentName);
			a.setLocation(appointmentLocation);
			a.setDescription(appointmentDescription);
			a.setStart(appointmentstart.getTime());
			a.setEnd(appointmentend.getTime());
			a.setIsDaily(isDaily);
			a.setIsWeekly(isWeekly);
			a.setIsMonthly(isMonthly);
			a.setIsYearly(isYearly);
			a.setCategory(appointmentCategoryDao.get(categoryId));
			a.setRemind(appointmentReminderTypDao.get(remind));
			a.getRoom().setComment(appointmentDescription);
			a.getRoom().setName(appointmentName);
			a.getRoom().setRoomtype(roomTypeDao.get(roomType));
			a.setOwner(userDao.get(users_id));
			a.setPasswordProtected(isPasswordProtected);
			a.setPassword(password);
			a.setMeetingMembers(new ArrayList<MeetingMember>());
			for (String singleClient : mmClient) {
				MeetingMember mm = appointmentLogic.getMeetingMember(users_id, languageId, singleClient);
				mm.setAppointment(a);
				a.getMeetingMembers().add(mm);
			}
			return appointmentDao.update(a, users_id).getId();
		} catch (Exception err) {
			log.error("[updateAppointment]", err);
			throw new AxisFault(err.getMessage());
		}
	}

	/**
	 * 
	 * delete a calendar event
	 * 
	 * If the given SID is from an Administrator or Web-Service user, the user
	 * can delete any appointment.<br/>
	 * If the SID is assigned to a simple user, he can only delete appointments
	 * where he is also the owner/creator of the appointment
	 * 
	 * @param SID
	 *            an authenticated SID
	 * @param appointmentId
	 *            the id to delete
	 * @param language_id
	 *            the language id in which the notifications for the deleted
	 *            appointment are send
	 * @return - id of appointment deleted
	 */
	public Long deleteAppointment(String SID, Long appointmentId, Long language_id) throws AxisFault {
		try {
			Long users_id = sessiondataDao.checkSession(SID);
			Set<Right> rights = userDao.getRights(users_id);

			Appointment a = appointmentDao.get(appointmentId);
			if (AuthLevelUtil.hasWebServiceLevel(rights) || AuthLevelUtil.hasAdminLevel(rights)) {
				// fine
			} else if (AuthLevelUtil.hasUserLevel(rights)) {
				// check if the appointment belongs to the current user
				if (!a.getOwner().getUser_id().equals(users_id)) {
					throw new AxisFault("The Appointment cannot be updated by the given user");
				}
			} else {
				throw new AxisFault("Not allowed to preform that action, Authenticate the SID first");
			}
			appointmentDao.delete(a, users_id); //FIXME check this !!!!
			return a.getId();
		} catch (Exception err) {
			log.error("[deleteAppointment]", err);
			throw new AxisFault(err.getMessage());
		}
	}

	/**
	 * 
	 * Load a calendar event by its room id
	 * 
	 * @param SID
	 * @param room_id
	 * @return - calendar event by its room id
	 */
	public AppointmentDTO getAppointmentByRoomId(String SID, Long room_id) {
		try {
			Long users_id = sessiondataDao.checkSession(SID);
			if (AuthLevelUtil.hasUserLevel(userDao.getRights(users_id))) {
				Appointment appStored = appointmentDao.getAppointmentByRoomId(users_id, room_id);
				if (appStored != null) {
					return new AppointmentDTO(appStored);
				}
			}
		} catch (Exception err) {
			log.error("[getAppointmentByRoomId]", err);
		}
		return null;
	}

	/**
	 * Get all categories of calendar events
	 * 
	 * @param SID
	 * @return - all categories of calendar events
	 */
	public List<AppointmentCategory> getAppointmentCategoryList(String SID) {
		log.debug("AppointmenetCategoryService.getAppointmentCategoryList SID : " + SID);

		try {
			Long users_id = sessiondataDao.checkSession(SID);

			if (AuthLevelUtil.hasUserLevel(userDao.getRights(users_id))) {
				List<AppointmentCategory> res = appointmentCategoryDao.getAppointmentCategoryList();

				if (res == null || res.size() < 1) {
					log.debug("no AppointmentCategories found");
				} else {
					for (int i = 0; i < res.size(); i++) {
						AppointmentCategory ac = res.get(i);
						log.debug("found appCategory : " + ac.getName());
					}
				}

				return res;
			} else {
				log.error("AppointmenetCategoryService.getAppointmentCategoryList : UserLevel Error");
			}
		} catch (Exception err) {
			log.error("[getAppointmentCategory]", err);
		}
		return null;

	}

	private List<AppointmentReminderTypeDTO> getReminders(List<AppointmentReminderTyps> list) {
		List<AppointmentReminderTypeDTO> result = new ArrayList<>(list.size());
		for (AppointmentReminderTyps rt : list) {
			result.add(new AppointmentReminderTypeDTO(rt));
		}
		return result;
	}
	/**
	 * Get all reminder types for calendar events
	 * 
	 * @param SID
	 * @return - all reminder types for calendar events
	 */
	public List<AppointmentReminderTypeDTO> getAppointmentReminderTypList(String SID) {
		log.debug("getAppointmentReminderTypList");

		try {
			Long users_id = sessiondataDao.checkSession(SID);
			if (AuthLevelUtil.hasUserLevel(userDao.getRights(users_id))) {

				User user = userDao.get(users_id);
				long language_id = (user == null) ? 1 : user.getLanguage_id();
				return getReminders(appointmentReminderTypDao.getAppointmentReminderTypList(language_id));
			} else
				log.debug("getAppointmentReminderTypList  :error - wrong authlevel!");
		} catch (Exception err) {
			log.error("[getAppointmentReminderTypList]", err);
		}
		return null;
	}
}
