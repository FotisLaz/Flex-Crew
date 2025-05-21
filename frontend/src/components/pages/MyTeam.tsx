import { useState } from 'react';
import Employees from '../../components/common/Employees';
import Grid from "../../components/common/Grid";
import Navbar from "../../components/common/Navbar";
import { BsSearch } from 'react-icons/bs';

const MyTeam = () => {
  const [searchTerm, setSearchTerm] = useState('');
  
  const handleSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(e.target.value);
  };

  return (
    <Grid>
      <Navbar />
      
      <div className="mx-4 my-6">
        <div className="mb-4 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <h1 className="text-3xl font-bold text-gray-800">Team Management</h1>
          
          <div className="relative w-full sm:w-72">
            <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
              <BsSearch className="text-gray-400" />
            </div>
            <input
              type="text"
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-yellow-FlexCrew focus:border-transparent"
              placeholder="Search team members..."
              value={searchTerm}
              onChange={handleSearch}
            />
          </div>
        </div>
        
        <Employees searchFilter={searchTerm} />
      </div>
    </Grid>
  );
};

export default MyTeam;
