import { useNavigate } from "react-router-dom";
import { BsBell, BsPersonCircle, BsPerson } from "react-icons/bs";

const AccountMenu = () => {
  const navigate = useNavigate();

  const navigateToProfile = () => {
    navigate("/profile");
  };

  return (
    <div className="flex flex-row justify-end items-center gap-6 text-xl text-gray-600">
      <button className="hover:text-yellow-FlexCrew transition-colors">
        <BsBell />
      </button>
      <button 
        onClick={navigateToProfile} 
        className="hover:text-yellow-FlexCrew transition-colors"
        title="My Profile"
      >
        <BsPersonCircle />
      </button>
    </div>
  );
};

export default AccountMenu;
