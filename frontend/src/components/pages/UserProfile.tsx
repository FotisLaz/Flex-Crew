import React, { useState, useEffect } from 'react';
import useAuth from '../../hooks/useAuth';
import useApiPrivate from '../../hooks/useApiPrivate';
import { Employee } from '../../types/Employee';
import { BsPerson, BsEnvelope, BsPeople, BsCalendar, BsPencil, BsKey, BsEye, BsEyeSlash } from 'react-icons/bs';
import Grid from '../common/Grid';
import Navbar from '../common/Navbar';
import Modal from '../common/Modal';

// Password validation regex (example: min 8 chars, 1 letter, 1 number)
const PWD_REGEX = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$/;

const UserProfile = () => {
  const { userEmail } = useAuth();
  const apiPrivate = useApiPrivate();
  const [employee, setEmployee] = useState<Employee | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [passwordSuccess, setPasswordSuccess] = useState<string | null>(null);
  const [showOldPassword, setShowOldPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isChangingPassword, setIsChangingPassword] = useState(false);

  useEffect(() => {
    let isMounted = true;
    const controller = new AbortController();

    const fetchEmployeeData = async () => {
      if (!userEmail) {
        setError('User email not found.');
        setLoading(false);
        return;
      }
      try {
        setLoading(true);
        // Use specific endpoint
        const response = await apiPrivate.get(`/api/employees/search?employeeEmail=${userEmail}`, {
          signal: controller.signal,
        });
        
        if (isMounted) {
           if (response.data) {
             setEmployee(response.data);
             setError(null);
           } else {
             setError('Employee data not found.');
           }
        }
      } catch (err: any) {
        console.error("Error fetching employee data:", err);
        if (isMounted) {
           setError(err.response?.data?.message || err.message || 'Failed to fetch user profile.');
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };

    fetchEmployeeData();

    return () => {
      isMounted = false;
      controller.abort();
    };
  }, [userEmail, apiPrivate]);

  const getFullName = (emp: Employee | null) => {
    if (!emp) return 'N/A';
    return `${emp.names} ${emp.firstSurname} ${emp.secondSurname}`.trim();
  };

  const openChangePasswordModal = () => {
    setOldPassword('');
    setNewPassword('');
    setConfirmPassword('');
    setPasswordError(null);
    setPasswordSuccess(null);
    setIsModalOpen(true);
  };

  const handlePasswordChange = async (e: React.FormEvent) => {
      e.preventDefault();
      setPasswordError(null);
      setPasswordSuccess(null);
      
      if (!PWD_REGEX.test(newPassword)) {
          setPasswordError('Password must be at least 8 characters long and contain at least one letter and one number.');
          return;
      }
      if (newPassword !== confirmPassword) {
          setPasswordError('New passwords do not match.');
          return;
      }
      if (!employee) {
          setPasswordError('User data not available.');
          return;
      }

      setIsChangingPassword(true);
      try {
          // *** TODO: Replace with actual API call ***
          console.log('Attempting to change password for:', employee.email);
          // Example API call structure:
          // await apiPrivate.put(`/api/employees/change-password`, { 
          //     email: employee.email, 
          //     oldPassword,
          //     newPassword 
          // });
          
          // Simulate API call delay and success/error
          await new Promise(resolve => setTimeout(resolve, 1500)); 
          // Simulate success for UI testing:
          setPasswordSuccess('Password changed successfully!');
          // Simulate error:
          // throw new Error("Incorrect old password (Simulated)"); 
          
          // Close modal on success after a short delay
          setTimeout(() => {
              setIsModalOpen(false);
          }, 2000); 

      } catch (err: any) {
          console.error("Password change error:", err);
          setPasswordError(err.response?.data?.message || err.message || 'Failed to change password.');
      } finally {
          setIsChangingPassword(false);
      }
  };

  if (loading) {
    return (
      <Grid>
        <Navbar />
        <div className="flex justify-center items-center h-full">
          <div className="animate-spin rounded-full h-16 w-16 border-t-2 border-b-2 border-yellow-FlexCrew"></div>
        </div>
      </Grid>
    );
  }

  if (error) {
    return (
       <Grid>
        <Navbar />
        <div className="flex flex-col items-center justify-center h-full p-6 bg-white rounded-lg shadow-md">
          <p className="text-red-500 text-xl">{error}</p>
        </div>
      </Grid>
    );
  }

  return (
    <Grid>
      <Navbar />
      <div className="p-6 bg-white rounded-lg shadow-md h-full flex flex-col">
        <h1 className="text-2xl font-bold mb-6 text-gray-800 border-b pb-3">My Profile</h1>
        
        {employee ? (
          <div className="flex flex-col gap-6 flex-grow">
             <div className="flex items-center gap-4">
                <div className="w-20 h-20 bg-gray-200 rounded-full flex items-center justify-center overflow-hidden border-2 border-yellow-FlexCrew">
                   {employee.imageUrl ? (
                     <img 
                       src={employee.imageUrl} 
                       alt={getFullName(employee)} 
                       className="w-full h-full object-cover"
                     />
                   ) : (
                     <BsPerson className="text-4xl text-gray-400" />
                   )}
                 </div>
                 <div>
                   <h2 className="text-xl font-semibold text-gray-900">{getFullName(employee)}</h2>
                    <p className="text-sm text-gray-500">{employee.role === 'ADMIN' ? 'Administrator' : 'User'}</p>
                 </div>
             </div>

             <div className="space-y-3 text-sm text-gray-700">
                <div className="flex items-center gap-2">
                  <BsEnvelope className="text-gray-500" />
                  <span>{employee.email}</span>
                </div>
                <div className="flex items-center gap-2">
                  <BsPeople className="text-gray-500" />
                  <span>Team: {employee.team?.name || 'N/A'}</span>
                </div>
                <div className="flex items-center gap-2">
                  <BsCalendar className="text-gray-500" />
                  <span>Schedule: {employee.schedule?.name || 'N/A'}</span>
                </div>
             </div>

             <div className="mt-auto pt-6 border-t flex flex-col sm:flex-row gap-4">
                 <button 
                   onClick={openChangePasswordModal}
                   className="flex items-center justify-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors text-sm"
                 >
                     <BsKey />
                     Change Password
                 </button>
                 <button className="flex items-center justify-center gap-2 px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 transition-colors text-sm disabled:opacity-50" disabled>
                     <BsPencil />
                     Edit Profile (Coming Soon)
                 </button>
             </div>

          </div>
        ) : (
          <p className="text-gray-500">Could not load profile information.</p>
        )}
      </div>

      {/* Change Password Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Change Password">
          <form onSubmit={handlePasswordChange} className="space-y-4">
              {passwordError && <p className="text-red-500 text-sm">{passwordError}</p>}
              {passwordSuccess && <p className="text-green-500 text-sm">{passwordSuccess}</p>}
              
              {/* Old Password */}
              <div className="relative">
                  <label htmlFor="oldPassword" className="block text-sm font-medium text-gray-700 mb-1">Old Password</label>
                  <input 
                      type={showOldPassword ? "text" : "password"} 
                      id="oldPassword"
                      value={oldPassword}
                      onChange={(e) => setOldPassword(e.target.value)}
                      required
                      className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  />
                  <button 
                      type="button"
                      onClick={() => setShowOldPassword(!showOldPassword)}
                      className="absolute inset-y-0 right-0 top-6 pr-3 flex items-center text-gray-500 hover:text-blue-600"
                  >
                      {showOldPassword ? <BsEyeSlash /> : <BsEye />} 
                  </button>
              </div>

              {/* New Password */}
              <div className="relative">
                   <label htmlFor="newPassword" className="block text-sm font-medium text-gray-700 mb-1">New Password</label>
                   <input 
                       type={showNewPassword ? "text" : "password"} 
                       id="newPassword"
                       value={newPassword}
                       onChange={(e) => setNewPassword(e.target.value)}
                       required
                       className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                   />
                   <button 
                       type="button"
                       onClick={() => setShowNewPassword(!showNewPassword)}
                       className="absolute inset-y-0 right-0 top-6 pr-3 flex items-center text-gray-500 hover:text-blue-600"
                   >
                       {showNewPassword ? <BsEyeSlash /> : <BsEye />}
                   </button>
                   <p className="text-xs text-gray-500 mt-1">Min 8 characters, 1 letter, 1 number.</p>
               </div>

               {/* Confirm New Password */} 
              <div className="relative">
                   <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-1">Confirm New Password</label>
                   <input 
                       type={showConfirmPassword ? "text" : "password"} 
                       id="confirmPassword"
                       value={confirmPassword}
                       onChange={(e) => setConfirmPassword(e.target.value)}
                       required
                       className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                   />
                   <button 
                       type="button"
                       onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                       className="absolute inset-y-0 right-0 top-6 pr-3 flex items-center text-gray-500 hover:text-blue-600"
                   >
                       {showConfirmPassword ? <BsEyeSlash /> : <BsEye />} 
                   </button>
               </div>

               <div className="flex justify-end pt-2">
                   <button 
                       type="button" 
                       onClick={() => setIsModalOpen(false)} 
                       className="px-4 py-2 mr-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300"
                       disabled={isChangingPassword}
                   >
                       Cancel
                   </button>
                   <button 
                       type="submit" 
                       className={`px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 flex items-center justify-center ${isChangingPassword ? 'cursor-not-allowed' : ''}`}
                       disabled={isChangingPassword || !oldPassword || !newPassword || !confirmPassword}
                   >
                       {isChangingPassword ? (
                          <div className="animate-spin rounded-full h-4 w-4 border-t-2 border-b-2 border-white mr-2"></div>
                       ) : null}
                       {isChangingPassword ? 'Changing...' : 'Change Password'}
                   </button>
               </div>
           </form>
      </Modal>
    </Grid>
  );
};

export default UserProfile; 