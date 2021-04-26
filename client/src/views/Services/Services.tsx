import React, { useCallback, useEffect, useState } from "react";

import { Button, Spinner } from "../../components";
import { useNotifications } from "../../notifications";
import { isValidUrl } from "../../utils";
import useInterval from "../../utils/useInterval";

import { useServices } from "./Ctx";
import { Service } from "./Service";

const CHECK_EVERY = 10;
export const Services: React.FC = () => {
  const { notify } = useNotifications();
  const { add: addService, services, set: setServices } = useServices();

  const [url, setUrl] = useState("");
  const [isAdding, setIsAdding] = useState(false);

  const [isChecking, setIsChecking] = useState(false);
  const [checkingIn, setCheckingIn] = useState(CHECK_EVERY);

  const fetchServices = useCallback(async () => {
    const response = await fetch("/api/services");
    if (response.ok) {
      setServices((await response.json()).services);
    } else {
      notify(
        "There has been a problem loading your services. Please, try again later.",
        "error",
      );
    }
  }, []);

  useEffect(() => {
    fetchServices();
  }, []);

  useInterval(
    async () => {
      if (checkingIn > 1) {
        setCheckingIn(checkingIn - 1);
      } else {
        await fetchServices();
        setCheckingIn(10);
      }
    },
    services && !isChecking ? 1000 : null,
  );

  return (
    <div>
      <h1 className="py-6 text-2xl font-bold tracking-widest text-center text-gray-400 uppercase">
        Service Health
      </h1>

      <p className="mb-16">
        Prevention is key! Check your services on a regular basis to avoid dire
        straits in the future.
      </p>

      <form
        className="flex flex-col items-center space-y-2"
        onSubmit={async (e) => {
          e.preventDefault();

          if (!url || !isValidUrl(url)) {
            notify(`Please, enter a valid URL.`, "error", "notification-add");
            return;
          }

          setIsAdding(true);
          const response = await fetch("/api/services", {
            method: "POST",
            body: JSON.stringify({ url }),
          });
          if (response.ok) addService((await response.json()).service);
          setIsAdding(false);
        }}
      >
        <input
          placeholder="https://google.com"
          type="text"
          value={url}
          onChange={(e) => {
            setUrl(e.target.value);
          }}
          className="w-full p-4 pr-20 text-white bg-gray-900 border-t-2 border-green-500 rounded shadow-inner outline-none"
        />
        <Button isLoading={isAdding} label="Add service" type="submit" />
      </form>

      {!services || isChecking ? (
        <Spinner size={24} className="mt-8" />
      ) : (
        <div className="flex flex-col p-6 mt-8 flex-stretch">
          {Object.keys(services).length < 1 ? (
            <p className="text-center text-gray-300">
              Nothing to check yet, simply add your services in the field above
              👆.
            </p>
          ) : (
            <>
              <div className="flex px-8 mb-6 font-bold">
                <span className="flex-grow px-2">Service</span>
                <span className="flex-grow-0 w-16 text-right">Status</span>
              </div>
              {Object.values(services).map((service) => (
                <Service key={service.id} {...service} />
              ))}
              <p className="relative mt-4 italic text-center text-gray-500">
                (Checking again in {checkingIn} seconds.{" "}
                <button
                  className="underline"
                  onClick={async (e) => {
                    setIsChecking(true);
                    await fetchServices();
                    setCheckingIn(CHECK_EVERY);
                    setIsChecking(false);
                  }}
                >
                  Check now
                </button>
                .)
              </p>
            </>
          )}
        </div>
      )}
    </div>
  );
};
