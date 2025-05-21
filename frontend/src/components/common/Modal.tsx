import React from 'react';
import { BsX } from 'react-icons/bs'; // Import close icon

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode; // Content of the modal
  maxWidth?: string; // Optional max width (e.g., 'max-w-md', 'max-w-lg')
}

const Modal: React.FC<ModalProps> = ({ isOpen, onClose, title, children, maxWidth = 'max-w-lg' }) => {
  if (!isOpen) return null;

  // Prevent closing when clicking inside the modal content
  const handleContentClick = (e: React.MouseEvent) => {
    e.stopPropagation();
  };

  return (
    // Backdrop
    <div 
      className="fixed inset-0 bg-black bg-opacity-50 backdrop-blur-sm flex justify-center items-center z-50 transition-opacity duration-300 ease-in-out"
      onClick={onClose} // Close when clicking the backdrop
    >
      {/* Modal Content */}
      <div 
        className={`bg-white rounded-lg shadow-xl w-full ${maxWidth} p-6 mx-4 flex flex-col transform transition-all duration-300 ease-in-out scale-95 opacity-0 animate-modal-fade-in`}
        onClick={handleContentClick} // Prevent backdrop click propagation
        style={{ maxHeight: '90vh' }} // Limit height and allow scrolling
      >
        {/* Header */}
        <div className="flex justify-between items-center border-b pb-3 mb-4">
          <h2 className="text-xl font-semibold text-gray-800">{title}</h2>
          <button 
            onClick={onClose}
            className="text-gray-500 hover:text-gray-800 p-1 rounded-full hover:bg-gray-200 transition-colors"
            aria-label="Close modal"
          >
            <BsX size={24} />
          </button>
        </div>

        {/* Body - Allow scrolling if content is too tall */}
        <div className="overflow-y-auto flex-grow">
          {children}
        </div>
      </div>
      
      {/* Basic Tailwind Animation - Add this to your global CSS or tailwind.config.js */}
      <style jsx global>{`
        @keyframes modal-fade-in {
          from { opacity: 0; transform: scale(0.95); }
          to { opacity: 1; transform: scale(1); }
        }
        .animate-modal-fade-in {
          animation: modal-fade-in 0.2s ease-out forwards;
        }
      `}</style>
    </div>
  );
};

export default Modal; 