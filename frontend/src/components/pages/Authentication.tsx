import teamImage from "../../assets/images/team-time.png";
import minimalLogo from "../../assets/images/logo-simple.png";
import LoginForm from "../common/LoginForm";
import RegisterForm from "../common/RegisterForm";
import { useState } from "react";

const Authentication = () => {
  const svgCode = `<svg width="695" height="1024" viewBox="0 0 695 1024" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M695 0V1024H75.9997C75.9997 1024 -121.5 206 122 476C365.5 746 695 0 695 0Z" fill="#EBB200"/></svg>`;

  const encodedSvg = btoa(svgCode);

  const svgBackground = {
    background: `url("data:image/svg+xml;base64,${encodedSvg}")`,
    backgroundColor: "black",
    backgroundRepeat: "no-repeat",
    backgroundPosition: "right center",
    backgroundSize: "contain",
  };

  const [whichForm, setWhichForm] = useState(true);
  const changeForm = () => {
    setWhichForm(!whichForm);
  };

  return (
    <div className="flex flex-row min-h-screen bg-black">
      <div className="flex flex-col flex-1 text-white gap-4 items-center justify-center py-8">
        <img src={minimalLogo} className="w-16 h-fit" alt="Logo" />

        {whichForm ? <LoginForm /> : <RegisterForm onRegistered={() => setWhichForm(true)} />}
        <div className="text-gray-500">
          {whichForm ? "Don't have an account? " : "Already registered? "}
          <a onClick={changeForm}>
            <span className="text-gray-300 hover:text-yellow-FlexCrew underline">
              {whichForm ? "Register" : "Login"}
            </span>
          </a>
        </div>
      </div>

      {/* The yellow shape is the background of this column only, so it can never cover the form. */}
      <div style={svgBackground} className="hidden lg:flex flex-1 items-center justify-center">
        <img src={teamImage} className="w-3/4 max-w-sm h-fit" alt="Team time" />
      </div>
    </div>
  );
};

export default Authentication;
