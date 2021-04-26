import React from "react";
import { Spinner } from ".";

export const Button: React.FC<{
  isLoading?: boolean;
  label: string;
  type?: "button" | "reset" | "submit";
}> = ({ isLoading = false, label, type = "button" }) => {
  return (
    <button
      className={`px-4 py-2 text-green-400 bg-gray-900 hover:text-green-300 focus:outline-none relative ${
        isLoading && "cursor-not-allowed"
      }`}
      disabled={isLoading}
      type={type}
    >
      {isLoading && (
        <Spinner size={6} stroke={2} className="absolute left-0 right-0" />
      )}
      <span className={`${isLoading ? "invisible" : ""}`}>{label}</span>
    </button>
  );
};
