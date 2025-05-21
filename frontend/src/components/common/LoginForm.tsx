import { useRef, useState, useEffect } from "react";
import {
  faCheck,
  faTimes,
  faInfoCircle,
  faEye,
  faEyeSlash,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { postAuth } from "../../services/postAuth";
import useAuth from "../../hooks/useAuth";
import { useNavigate, useLocation } from "react-router-dom";
import { postRecord } from "../../services/postRecord";

const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
// Relaxed password regex for development purposes
const PWD_REGEX = /^(?=.*[a-zA-Z])(?=.*[0-9]).{8,24}$/;

const LoginForm = () => {
  const { login } = useAuth();

  const navigate = useNavigate();
  const location = useLocation();
  // Get the path the user was trying to access, or fallback to the dashboard
  const from = location.state?.from?.pathname || "/";

  const userRef = useRef<HTMLInputElement | null>(null);
  const errRef = useRef<HTMLParagraphElement | null>(null);

  const [userEmail, setUserEmail] = useState("");
  const [validName, setValidName] = useState(false);
  const [userFocus, setUserFocus] = useState(false);

  const [pwd, setPwd] = useState("");
  const [validPwd, setValidPwd] = useState(false);
  const [pwdFocus, setPwdFocus] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const [errMsg, setErrMsg] = useState("");

  useEffect(() => {
    userRef.current?.focus();
  }, []);

  useEffect(() => {
    setValidName(EMAIL_REGEX.test(userEmail));
  }, [userEmail]);

  useEffect(() => {
    setValidPwd(PWD_REGEX.test(pwd));
  }, [pwd]);

  useEffect(() => {
    setErrMsg("");
  }, [userEmail, pwd]);

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    // if button enabled with JS hack
    e.preventDefault();
    const emailValidation = EMAIL_REGEX.test(userEmail);
    const pwdValidation = PWD_REGEX.test(pwd);

    if (!emailValidation || !pwdValidation) {
      setErrMsg("Invalid Entry");
      return;
    }

    try {
      // Get the authentication response data directly
      const authData = await postAuth(userEmail, pwd);
      
      // Extract data directly from the returned object
      const accessToken = authData?.access_token;
      const refreshToken = authData?.refresh_token;
      const role = authData?.role; // Get the role from the response data

      // Check if essential data is present
      if (!accessToken || !refreshToken || !role) {
        console.error("Login response missing essential data:", authData);
        setErrMsg("Login failed: Incomplete data received from server.");
        return;
      }

      // Log the received role for debugging
      console.log("Role received from backend:", role);
      
      // Set auth context: pass tokens, the email entered in the form, and the role from response
      login(accessToken, refreshToken, userEmail, role); 
      
      // Post a record every login (Assuming postRecord uses the email from the context or token)
      // If postRecord needs the token directly, ensure it's passed correctly
      // postRecord(userEmail, accessToken); // Consider if this is still needed or handled differently

      // Clear state and controlled inputs
      setUserEmail("");
      setPwd("");
      
      console.log("Navigating to:", from);
      // Go to the path which user came from
      navigate(from, { replace: true });
    } catch (err: any) { // Changed type to any for broader error handling
      if (err?.response) {
        // Handle specific backend errors if available
        const status = err.response.status;
        const errorData = err.response.data;
        console.error(`Login failed with status ${status}:`, errorData);
        if (status === 403 || status === 401) { // 401 might also mean bad credentials
          setErrMsg("Incorrect email or password");
        } else {
          setErrMsg(errorData?.message || "Login failed. Please try again.");
        }
      } else if (err.request) {
        // The request was made but no response was received
        console.error("No server response:", err.request);
        setErrMsg("No Server response. Check connection or server status.");
      } else {
        // Something happened in setting up the request that triggered an Error
        console.error("Error setting up login request:", err.message);
        setErrMsg("Login Error: Could not send request.");
      }
      errRef.current?.focus();
    }
  };

  return (
    <>
      <p
        ref={errRef}
        className={errMsg ? "errmsg text-red-300" : "offscreen"}
        aria-live="assertive"
      >
        {errMsg}
      </p>

      <span className="text-3xl">WELCOME</span>
      <form
        className="max-w-md mx-auto flex flex-col items-stretch w-5/12"
        onSubmit={handleSubmit}
      >
        <div className="relative z-0 w-full mb-5 group">
          <input
            type="email"
            name="floating_email"
            id="floating_email"
            placeholder=" "
            required
            autoComplete="off"
            ref={userRef}
            onChange={(e) => setUserEmail(e.target.value)}
            value={userEmail}
            aria-invalid={validName ? "false" : "true"}
            aria-describedby="uidnote"
            onFocus={() => setUserFocus(true)}
            onBlur={() => setUserFocus(false)}
            className="block py-2.5 px-0 w-full text-sm bg-transparent border-0 border-b-2 border-gray-300 appearance-none text-white dark:border-gray-600 dark:focus:border-yellow-500 focus:outline-none focus:ring-0 focus:border-yellow-600 peer"
          />
          <label
            htmlFor="floating_email"
            className="peer-focus:font-medium absolute text-sm text-gray-400 duration-300 transform -translate-y-6 scale-75 top-3 -z-10 origin-[0] peer-focus:start-0 rtl:peer-focus:translate-x-1/4 rtl:peer-focus:left-auto peer-focus:text-yellow-600 peer-focus:dark:text-yellow-500 peer-placeholder-shown:scale-100 peer-placeholder-shown:translate-y-0 peer-focus:scale-75 peer-focus:-translate-y-6"
          >
            Email address
            <FontAwesomeIcon
              icon={faCheck}
              className={validName ? "visible text-green-200 ml-2" : "hidden"}
            />
            <FontAwesomeIcon
              icon={faTimes}
              className={
                validName || !userEmail ? "hidden" : "visible text-red-400 ml-2"
              }
            />
          </label>
          <p
            id="uidnote"
            className={
              userFocus && userEmail && !validName
                ? "visible text-xs"
                : "hidden"
            }
          >
            <FontAwesomeIcon icon={faInfoCircle} className="mr-2" />
            Must be a valid email address
            <br />
          </p>
        </div>

        <div className="relative z-0 w-full mb-5 group">
          <input
            type={showPassword ? "text" : "password"}
            name="floating_password"
            id="floating_password"
            className="block py-2.5 px-0 w-full text-sm bg-transparent border-0 border-b-2 border-gray-300 appearance-none text-white dark:border-gray-600 dark:focus:border-yellow-500 focus:outline-none focus:ring-0 focus:border-yellow-600 peer"
            placeholder=" "
            required
            onChange={(e) => setPwd(e.target.value)}
            value={pwd}
            aria-invalid={validPwd ? "false" : "true"}
            aria-describedby="pwdnote"
            onFocus={() => setPwdFocus(true)}
            onBlur={() => setPwdFocus(false)}
          />
          <button
            type="button"
            onClick={togglePasswordVisibility}
            className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-yellow-FlexCrew"
            tabIndex={-1}
          >
            <FontAwesomeIcon icon={showPassword ? faEyeSlash : faEye} />
          </button>
          <label
            htmlFor="floating_password"
            className="peer-focus:font-medium absolute text-sm text-gray-400 duration-300 transform -translate-y-6 scale-75 top-3 -z-10 origin-[0] peer-focus:start-0 rtl:peer-focus:translate-x-1/4 peer-focus:text-yellow-600 peer-focus:dark:text-yellow-500 peer-placeholder-shown:scale-100 peer-placeholder-shown:translate-y-0 peer-focus:scale-75 peer-focus:-translate-y-6"
          >
            Password
            <FontAwesomeIcon
              icon={faCheck}
              className={validPwd ? "visible text-green-200 ml-2" : "hidden"}
            />
            <FontAwesomeIcon
              icon={faTimes}
              className={
                validPwd || !pwd ? "hidden" : "visible text-red-400 ml-2"
              }
            />
          </label>
          <p
            id="pwdnote"
            className={pwdFocus && !validPwd ? "visible text-xs" : "hidden"}
          >
            <FontAwesomeIcon icon={faInfoCircle} className="mr-2" />
            8 to 24 characters.
            <br />
            Must include at least one letter and one number.
          </p>
        </div>

        <button
          className="text-black bg-yellow-FlexCrew hover:bg-yellow-600 focus:ring-1 focus:outline-none focus:ring-yellow-300 font-medium rounded-lg text-sm w-full sm:w-auto px-5 py-2.5 text-center"
          // disabled={!validName || !validPwd ? true : false}
        >
          LOGIN
        </button>
      </form>
    </>
  );
};

export default LoginForm;
