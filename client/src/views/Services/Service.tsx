import React, { useMemo, useState } from "react";

import { Spinner } from "../../components";
import { useNotifications } from "../../notifications";
import { isValidUrl } from "../../utils";

import { useServices, IService } from "./Ctx";

export const Service: React.FC<IService> = ({
  id,
  status,
  url: originalUrl,
}) => {
  const { notify } = useNotifications();
  const { remove, updateUrl } = useServices();
  const [url, setUrl] = useState(originalUrl);

  const [isEditing, setIsEditing] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const isFail = useMemo(() => status === "fail", [status]);

  return (
    <div
      key={id}
      className={`relative flex items-center rounded-sm h-12 px-2 ${
        isFail ? "bg-red-800 hover:bg-red-500" : "hover:bg-gray-700"
      }  group`}
    >
      {isLoading ? (
        <Spinner size={6} stroke={4} />
      ) : (
        <>
          <input
            className={`flex-grow px-2 py-1 outline-none focus:outline-none rounded-sm w-auto ${
              isEditing ? "bg-white text-black" : "bg-transparent"
            } border-0`}
            title="Click to edit"
            value={url}
            onFocus={() => setIsEditing(true)}
            onBlur={() => setIsEditing(false)}
            onChange={(e) => setUrl(e.target.value)}
            onKeyDown={async (e) => {
              if (e.key === "Enter") {
                setIsEditing(false);

                if (!isValidUrl(url)) {
                  notify("Please, enter a valid URL.", "error");
                  setUrl(originalUrl);
                  return;
                }

                setIsLoading(true);
                const response = await fetch(`/api/services/${id}`, {
                  method: "PUT",
                  body: JSON.stringify({ url }),
                });
                if (response.ok) updateUrl(id, url);
                setIsLoading(false);
              }
            }}
          ></input>
          <span className="flex-grow-0 w-16 pl-2 text-right">
            {status ?? "..."}
          </span>
          <button
            title="Click to remove"
            onClick={async () => {
              setIsLoading(true);
              const response = await fetch(`/api/services/${id}`, {
                method: "DELETE",
              });
              if (response.ok) remove(id);
              setIsLoading(false);
            }}
            className={`top-0 bottom-0 right-0 w-8 text-xl ${
              isFail ? "text-white" : "text-red-600"
            } opacity-0 absolut group-hover:opacity-100`}
          >
            <span>×</span>
          </button>
        </>
      )}
    </div>
  );
};
